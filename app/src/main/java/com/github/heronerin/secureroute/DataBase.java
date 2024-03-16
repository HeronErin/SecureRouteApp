package com.github.heronerin.secureroute;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.heronerin.secureroute.interactions.Event;

import java.io.File;
import java.net.URI;

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
                "note_data TEXT," +
                "image_uri TEXT);");
    }

    public synchronized void addEvent(Event event) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("variety", event.variety.toString());
            values.put("event_id", event.eventId.toString());
            values.put("timestamp", event.timeStamp);
            values.put("expense_value", event.expenseValue);

            if (event.noteData != null)
                values.put("note_data", event.noteData);

            if (event.imageUri != null)
                values.put("image_uri", event.imageUri.toString());

            db.insert("events", null, values);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implementation for database upgrades if needed
    }
}