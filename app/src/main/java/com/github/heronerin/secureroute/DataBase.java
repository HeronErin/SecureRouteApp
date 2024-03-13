package com.github.heronerin.secureroute;

import android.content.Context;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

import okio.Path;

public class DataBase {
    URI databaseUri;
    URI picturesUri;
    URI events;
    DataBase(Context context){
        File filesDir = context.getFilesDir();
        databaseUri = filesDir.toURI().resolve("database");
        picturesUri = databaseUri.resolve("pictures");
        events = databaseUri.resolve("events");


        (new  File(databaseUri)).mkdirs();
        (new  File(picturesUri)).mkdirs();
        (new  File(events)).mkdirs();
    }
}
