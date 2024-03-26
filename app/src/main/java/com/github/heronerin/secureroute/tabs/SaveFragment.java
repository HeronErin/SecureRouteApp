package com.github.heronerin.secureroute.tabs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.TripUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save, container, false);

        view.findViewById(R.id.exportDB).setOnClickListener(this::onExportDBButtonClick);

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_RES && resultCode == Activity.RESULT_OK)
            DataBase.getOrCreate(this.getContext()).exportDBToZip(this.getContext(), data.getData());


    }
}