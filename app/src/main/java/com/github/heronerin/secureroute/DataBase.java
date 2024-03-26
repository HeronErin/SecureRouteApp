package com.github.heronerin.secureroute;

import android.app.backup.FileBackupHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
        FileBackupHelper hosts = new FileBackupHelper(context, context.getDatabasePath(DB_NAME).toString());
    }
    private static void _mkEvents(SQLiteDatabase db){
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
    }
    private static void _mkImgs(SQLiteDatabase db){
        db.execSQL("CREATE TABLE images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uri TEXT," +
                "uuid TEXT UNIQUE);");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        _mkEvents(db);
        _mkImgs(db);
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
            addEvent(db, event);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    private void addEvent(SQLiteDatabase db, Event event){
        try {
            JSONArray truImgs = new JSONArray();
            JSONArray images = event.getImageData();
            for (int i = 0; i < images.length(); i++) {
                String uri = images.getJSONArray(i).getString(0);
                String uuid = UUID.randomUUID().toString();

                ContentValues image_values = new ContentValues();
                image_values.put("uri", uri);
                image_values.put("uuid", uuid);
                db.insert("images", null, image_values);

                JSONArray truImg = new JSONArray();
                truImg.put("!U! "+uuid);
                truImg.put(images.getJSONArray(i).getString(1));
                truImgs.put(truImg);

            }
            event.setImageData(truImgs);
        }catch (JSONException ignored) { }


        long id = _addEvent(db, event);
        customEventSaveHandler(event, id, db);
    }
    private long _addEvent(SQLiteDatabase db, Event event){
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
        }
        return db.insert("events", null, values);
    }
    private synchronized boolean writeDBToFile(Context context, OutputStream outputStream){
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
        if (oldVersion == 2)
            _mkImgs(db);
    }

    private List<Event> handleEventDiffs(SQLiteDatabase temp_db, boolean addToNewDb){
        Cursor cursor = temp_db.rawQuery( "SELECT DISTINCT event_id FROM events", null);
        Set<String> uniqueEventIds = new HashSet<>();
        while (cursor.moveToNext()) {
            uniqueEventIds.add(cursor.getString(0));
        }
        cursor.close();

        SQLiteDatabase mydb = getReadableDatabase();
        cursor = mydb.rawQuery("SELECT event_id FROM events", null);
        while (cursor.moveToNext()) {
            uniqueEventIds.remove(cursor.getString(0));
        }
        cursor.close();

        List<Event> events = new ArrayList<>();

        cursor = temp_db.rawQuery("SELECT * FROM events WHERE event_id IN ("+StringPlaceHolderGen(uniqueEventIds.size())+")", uniqueEventIds.toArray(new String[0]));
        while (cursor.moveToNext()) {
            try {
                Event e = eventFromCursor(cursor);
                if (addToNewDb)
                    _addEvent(mydb, e);

                events.add(e);
            } catch (JSONException e) {
                cursor.close();
                mydb.close();

                throw new RuntimeException(e);
            }
        }
        cursor.close();
        mydb.close();

        return events;
    }
    public synchronized JSONArray resolveImgUris(JSONArray uris) throws JSONException {
        try (SQLiteDatabase db = getReadableDatabase()){
            JSONArray ret = new JSONArray();
            for (int i = 0; i < uris.length(); i++) {
                String possibleUri = uris.getJSONArray(i).getString(0);
                String title = uris.getJSONArray(i).getString(1);
                if (possibleUri.startsWith("!U! "))
                    possibleUri = possibleUri.substring("!U! ".length());
                else{
                    ret.put(uris.getJSONArray(i));
                    continue;
                }
                Cursor cursor = db.rawQuery("SELECT uri FROM images WHERE uuid = ? LIMIT 1", new String[]{possibleUri});
                if (!cursor.moveToFirst()){
                    Log.e("DB", "Can't resolve image UUID: " + possibleUri);
                    continue;
                }
                JSONArray newImgJso = new JSONArray();
                newImgJso.put(cursor.getString(0));
                newImgJso.put(title);
                ret.put(newImgJso);

                cursor.close();

            }
            return ret;
        }
    }
    private List<String> eventsToUris(List<Event> events){
        List<String> uris = new ArrayList<>();
        try {
            for (Event event : events) {
                JSONArray images = event.getImageData();
                for (int i = 0; i < images.length(); i++) {
                    String[] split = images.getJSONArray(i).getString(0).split("/");
                    String number = split[split.length-1];

                    uris.add(number);
                }
            }
        }catch (JSONException ignored) { }
        return uris;
    }
    public synchronized boolean rebuiltByZipFile(Context context, Uri zipPath, boolean doCombine){
        SQLiteDatabase temp_db = null;
        List<String> uris_required = null;

        try(ZipInputStream input = new ZipInputStream(context.getContentResolver().openInputStream(zipPath))){
            ZipEntry zipEntry;
            OutputStream fileOutputStream = null;
            File file = null;
            while(null != (zipEntry = input.getNextEntry())){
                if (zipEntry.getName().equals("images/")) continue;

                try{
                    if (temp_db == null && !zipEntry.getName().equals("database.db")){
                        Toast.makeText(context, "Can't import database", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    file = File.createTempFile("", null);
                    fileOutputStream = new FileOutputStream(file);
                    TripUtils.copy(input, fileOutputStream);

                    if (temp_db == null && zipEntry.getName().equals("database.db")){
                        temp_db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                        if (temp_db.getVersion() != DB_VERSION)
                            onUpgrade(temp_db, temp_db.getVersion(), DB_VERSION);

                        uris_required = eventsToUris(handleEventDiffs(temp_db, doCombine));
                        continue;
                    }
                    if (!zipEntry.getName().startsWith("images/")) continue;
                    String number = zipEntry.getName();
                    if (number.contains(".")) number=number.split("\\.")[0];

                    if (!uris_required.contains(number)) continue;

                    uris_required.remove(number);


                }finally {
                    if (file != null && !zipEntry.getName().equals("database.db"))
                        file.delete();
                    if (fileOutputStream != null)
                        fileOutputStream.close();
                }
            }



        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;

    }
    public synchronized void exportDBToZip(Context context, Uri exportTo){
        try (ZipOutputStream out =  new ZipOutputStream(context.getContentResolver().openOutputStream(exportTo, "w"));){
            out.putNextEntry(new ZipEntry("database.db"));

            writeDBToFile(context, out);
            out.putNextEntry(new ZipEntry("images/"));
            for (String uri : getAllImageUris()){
                String[] split = uri.split("/");
                String number = split[split.length-1];

                out.putNextEntry(new ZipEntry("images/" + number));
                try(InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uri))){
                    TripUtils.copy(inputStream, out);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}