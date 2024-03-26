package com.github.heronerin.secureroute.tabs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.TripUtils;
import com.github.heronerin.secureroute.events.EventEditUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SaveFragment extends Fragment {
    public SaveFragment() {
        // Required empty public constructor
    }

    public static SaveFragment newInstance() {
        SaveFragment fragment = new SaveFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private static final int EXPORT_RES = 5234;
    void onExportDBButtonClick(View v){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip"); // Set the MIME type of the file you want to create
        intent.putExtra(Intent.EXTRA_TITLE, "routeDatabaseBackup.zip");
        startActivityForResult(intent, EXPORT_RES);
    }

    private static final int IMPORT_RES = 89234;

    void onImportDBButtonClick(View v){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip"); // Set the MIME type of the file you want to create
        intent.putExtra(Intent.EXTRA_TITLE, "routeDatabaseBackup.zip");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, IMPORT_RES);
    }
    void onUserImportSelector(Uri zipfile, boolean doReplace){
        EventEditUtils.confirm(()->
                DataBase.getOrCreate(this.getContext()).rebuiltByZipFile(getContext(), zipfile, doReplace),
                "Are your sure?",
                    doReplace
                            ? "This WILL do lasting damage to your database! Replacing the database involves DELETING THE DATABASE and replacing it!"
                            : "Merging the database may cause issues, and is a CPU intensive task.",
                this.getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save, container, false);

        view.findViewById(R.id.exportDB).setOnClickListener(this::onExportDBButtonClick);
        view.findViewById(R.id.importDB).setOnClickListener(this::onImportDBButtonClick);
        SharedPreferences sp = getContext().getSharedPreferences("info", Context.MODE_PRIVATE);

        String lastEdit = "Last edited at: " + (new Date(sp.getLong("last edited", 0))).toString();
        ((TextView)view.findViewById(R.id.lastSaved)).setText(lastEdit);

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_RES && resultCode == Activity.RESULT_OK)
            DataBase.getOrCreate(this.getContext()).exportDBToZip(this.getContext(), data.getData());
        if (requestCode == IMPORT_RES && resultCode == Activity.RESULT_OK)
            EventEditUtils.thisOrThat(
                    "Merge or Replace", "Would you like to merge with you current database, or replace it?",
                    this.getContext(),
                    "Merge", ()->onUserImportSelector(data.getData(), false),
                    "Replace", ()->onUserImportSelector(data.getData(), true)
            );


    }
}