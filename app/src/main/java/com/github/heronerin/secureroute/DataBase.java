package com.github.heronerin.secureroute;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
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
    URI picturesUri;
    File events;

    private static final String DB_NAME = "SecureRouteDB";
    private static final int DB_VERSION = 1;

    DataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        File filesDir = context.getFilesDir();
        databaseUri = filesDir.toURI().resolve("database");
        picturesUri = databaseUri.resolve("pictures");
        events = new File(databaseUri.resolve("events"));

        new File(databaseUri).mkdirs();
        new File(picturesUri).mkdirs();
        events.mkdirs();

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
                "note_data TEXT," +
                "image_uri TEXT);");
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
            values.put("expense_value", event.expenseValue);

            if (event.noteData != null)
                values.put("note_data", event.noteData);

            if (event.getImageData() != null)
                values.put("image_uri", event.getImageData().toString());

            long id = db.insert("events", null, values);
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
                cursor.getString(cursor.getColumnIndexOrThrow("image_uri")) != null ? new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))) : null
        );
        event.databaseId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        return event;
    }
    private synchronized List<Event> eventsBySql(String sql, @Nullable String[] args, int limit){
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
        // Implementation for database upgrades if needed
    }
}