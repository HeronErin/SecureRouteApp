package com.github.heronerin.secureroute;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataBase extends SQLiteOpenHelper {
    public static DataBase instance = null;
    public static DataBase getOrCreate(Context c) {
        if (instance == null)
            instance = new DataBase(c);
        return instance;
    }
    URI databaseUri;
    private static final String DB_NAME = "SecureRouteDB";
    private static final int DB_VERSION = 3;

    DataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "variety VARCHAR(255) NOT NULL," +
                "event_id UUID NOT NULL," +
                "timestamp BIGINT NOT NULL, " +
                "expense_value DOUBLE PRECISION," +
                "associated_pair INTEGER," +
                "odometer BIGINT," +
                "note_data TEXT," +
                "image_uri TEXT);");
        db.execSQL("CREATE TABLE images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uri TEXT UNIQUE);");
    }

    @Nullable
    public Event getLastTrip(){
        String[] possible = new String[]{Event.EventVariety.TripStart.toString(), Event.EventVariety.TripEnd.toString()};
        List<Event> events = eventsBySql(
                "SELECT * FROM events WHERE variety in ("+StringPlaceHolderGen(possible.length)+") ORDER BY timestamp DESC LIMIT 1", possible, Integer.MAX_VALUE
        );
        if (events.isEmpty())
            return null;
        return events.get(0);
    }

    @Nullable
    public Event getLastGas(){
        List<Event> events = eventsBySql(
                "SELECT * FROM events WHERE variety = ? ORDER BY timestamp DESC LIMIT 1", new String[]{Event.EventVariety.GasEvent.toString()}, Integer.MAX_VALUE
        );
        if (events.isEmpty())
            return null;
        return events.get(0);
    }
    @Nullable
    public Event getLastWithOdometer(){
        String[] possible = new String[]{Event.EventVariety.TripStart.toString(), Event.EventVariety.TripEnd.toString(), Event.EventVariety.GasEvent.toString(), Event.EventVariety.MillageStartJob.toString(), Event.EventVariety.MillageEndJob.toString(), Event.EventVariety.MillageStartNonJob.toString(), Event.EventVariety.MillageEndNonJob.toString()};
        List<Event> events = eventsBySql(
                "SELECT * FROM events WHERE variety in ("+StringPlaceHolderGen(possible.length)+") ORDER BY timestamp DESC LIMIT 1", possible, Integer.MAX_VALUE
        );
        if (events.isEmpty())
            return null;
        return events.get(0);
    }
    public List<Event> getEventsOfVariety(Event.EventVariety[] varieties, int limit){
        List<String> varietiesStrings = new ArrayList<>();
        for (Event.EventVariety variety : varieties)
            varietiesStrings.add(variety.toString());
        return eventsBySql(
                "SELECT * FROM events WHERE variety in ("+StringPlaceHolderGen(varietiesStrings.size())+") ORDER BY timestamp DESC",
                varietiesStrings.toArray(new String[]{}),
                limit
        );
    }
    public Event getEventAfterTimeOfType(Event.EventVariety variety, long timestamp){
        List<Event> events = eventsBySql(
                "SELECT * FROM events WHERE (variety = ? AND timestamp > ?) ORDER BY timestamp ASC LIMIT 1",
                new String[]{
                        variety.toString(),
                        String.valueOf(timestamp)
                }, Integer.MAX_VALUE
        );
        if (events.isEmpty())
            return null;
        return events.get(0);
    }
    @Nullable
    public Event getEventById(int id){
        List<Event> events = eventsBySql(
                "SELECT * FROM events WHERE id = ? LIMIT 1", new String[]{String.valueOf(id)}, Integer.MAX_VALUE
        );
        if (events.isEmpty())
            return null;
        return events.get(0);
    }
    public List<Event> getInTimeFrame(long start, long end){
        return eventsBySql(
                "SELECT * FROM events WHERE (timestamp > ? AND timestamp < ?) ORDER BY timestamp DESC",
                new String[]{String.valueOf(start), String.valueOf(end)},
                Integer.MAX_VALUE
        );
    }

    private static String StringPlaceHolderGen(int amount){
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        while(amount-- != 0){
            if (!isFirst)
                stringBuilder.append(", ");
            stringBuilder.append("?");
            isFirst = false;
        }
        return stringBuilder.toString();
    }
    public void customEventSaveHandler(Event event, long id, SQLiteDatabase db){
        if (event.variety == Event.EventVariety.ArbitraryRangeEnd){
            ContentValues values = new ContentValues();
            values.put("associated_pair", (int) id);
            db.update("events", values, "id = ?", new String[]{String.valueOf(event.associatedPair)});
        }
        if (event.variety == Event.EventVariety.TripEnd){
            ContentValues values = new ContentValues();
            values.put("associated_pair", (int) id);
            db.update("events", values, "id = ?", new String[]{String.valueOf(event.associatedPair)});
        }
        if (event.variety == Event.EventVariety.GasEventEnd){
            ContentValues values = new ContentValues();
            values.put("associated_pair", (int) id);
            db.update("events", values, "id = ?", new String[]{String.valueOf(event.associatedPair)});
        }

    }

    public synchronized void addEvent(Event event) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("variety", event.variety.toString());
            values.put("event_id", event.eventId.toString());
            values.put("timestamp", event.timeStamp);
            values.put("associated_pair", event.associatedPair);
            values.put("expense_value", event.moneyAmount);
            values.put("odometer", event.odometer);

            if (event.noteData != null)
                values.put("note_data", event.noteData);

            if (event.getImageData() != null) {
                JSONArray images = event.getImageData();
                values.put("image_uri", images.toString());

                try {
                    for (int i = 0; i < images.length(); i++) {
                        String uri = images.getJSONArray(i).getString(0);
                        ContentValues image_values = new ContentValues();
                        image_values.put("uri", uri);
                        db.insert("images", null, image_values);

                    }
                }catch (JSONException ignored) { }
            }

            long id = db.insert("events", null, values);
            customEventSaveHandler(event, id, db);

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    public synchronized boolean writeDBToFile(Context context, OutputStream outputStream){
        File DBPath = context.getDatabasePath(getDatabaseName());
        if (!DBPath.exists()) return false;

        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(DBPath))){
            TripUtils.copy(bufferedInputStream, outputStream);
            return true;
        }catch (IOException ignored){ }
        return false;
    }
    public synchronized void deleteEvent(Event event){
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete("events", "id = ?", new String[]{String.valueOf(event.databaseId)});
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    public synchronized void updateEvent(Event event) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("variety", event.variety.toString());
            values.put("event_id", event.eventId.toString());
            values.put("timestamp", event.timeStamp);
            values.put("associated_pair", event.associatedPair);
            values.put("expense_value", event.moneyAmount);
            values.put("odometer", event.odometer);

            if (event.noteData != null)
                values.put("note_data", event.noteData);

            if (event.getImageData() != null)
                values.put("image_uri", event.getImageData().toString());


            long id = db.update("events", values, "id = ?", new String[]{String.valueOf(event.databaseId)});
            customEventSaveHandler(event, id, db);

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    private Event eventFromCursor(Cursor cursor) throws JSONException {
        Event event = new Event(
                Event.EventVariety.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("variety"))),
                UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("event_id"))),
                cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("expense_value")),
                cursor.getInt(cursor.getColumnIndexOrThrow("associated_pair")),
                cursor.getString(cursor.getColumnIndexOrThrow("note_data")),
                cursor.getString(cursor.getColumnIndexOrThrow("image_uri")) != null ? new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))) : null,

                cursor.getLong(cursor.getColumnIndexOrThrow("odometer"))
        );
        event.databaseId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        return event;
    }
    public synchronized List<String> getAllImageUris(){
        List<String> strings = new ArrayList<>();
        try(SQLiteDatabase db = this.getReadableDatabase()){
            try(Cursor cursor = db.rawQuery("SELECT uri from images", null)) {
                if (!cursor.moveToFirst()) return strings;
                do {
                    strings.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }
        return strings;
    }

    private List<Event> eventsBySql(String sql, @Nullable String[] args, int limit){
        List<Event> recentEvents = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(sql, args);
            if (cursor.moveToFirst()) {
                do {
                    recentEvents.add(eventFromCursor(cursor));
                } while (cursor.moveToNext() && 0!=--limit);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return recentEvents;
    }
    public List<Event> getEventsByTime(int limit, boolean reverse){
        return eventsBySql("SELECT * FROM events ORDER BY timestamp " + (reverse ? "DESC" : "ASC"), null, limit);
    }
    public List<Event> getRange(int limit, boolean reverse){
        return eventsBySql("SELECT * FROM events " +
                "WHERE variety IN (" +  StringPlaceHolderGen(Event.possibleRangeStrings.length)+ ") "+
                "ORDER BY timestamp " + (reverse ? "DESC" : "ASC"), Event.possibleRangeStrings, limit);
    }
    public List<Event> getUnEndedRanges(int limit, boolean reverse){
        return eventsBySql("SELECT * FROM events " +
                "WHERE (variety IN (" +  StringPlaceHolderGen(Event.possibleRangeStrings.length)+ ") "+
                "AND associated_pair = -1 ) "+
                "ORDER BY timestamp " + (reverse ? "DESC" : "ASC"), Event.possibleRangeStrings, limit);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {



        if (oldVersion == 2 && newVersion == 3)
            db.execSQL("CREATE TABLE images (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uri TEXT UNIQUE);");


    }
}