package com.github.heronerin.secureroute.tabs;

import static com.github.heronerin.secureroute.GoogleDriveHelper.GOOGLE_SIGNIN;
import static com.github.heronerin.secureroute.GoogleDriveHelper.signIn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.GoogleDriveHelper;
import com.github.heronerin.secureroute.ManageBackups;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.EventEditUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.util.Date;

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

        String lastEdit = "Last edited at: " + new Date(sp.getLong("last edited", 0));
        ((TextView)view.findViewById(R.id.lastSaved)).setText(lastEdit);
        String lastUpload = "Last uploaded at: " + new Date(sp.getLong("last uploaded", 0));
        ((TextView)view.findViewById(R.id.lastUploaded)).setText(lastUpload);

        modifyGoogleBtn(view.findViewById(R.id.accountsGoogleBtn));
        view.findViewById(R.id.backUpNow).setOnClickListener((v)->{
            if (GoogleSignIn.getLastSignedInAccount(getContext()) == null){
                Toast.makeText(getContext(), "Please login to google to backup", Toast.LENGTH_SHORT).show();
                return;
            }
            backup();
        });

        return view;
    }

    private void modifyGoogleBtn(Button button){
        boolean accountNotFound = GoogleSignIn.getLastSignedInAccount(getContext()) == null;
        button.setBackgroundColor(
                accountNotFound ? Color.RED : Color.GREEN
        );
        button.setText(
                accountNotFound ? "Accounts" : "Backups"
        );
        if (accountNotFound)
            button.setOnClickListener((v)->signIn(getActivity()));
        else
            button.setOnClickListener((v)->startActivity(new Intent(getContext(), ManageBackups.class)));
    }
    public void backup(){
        Toast.makeText(getContext(), "Starting backup upload", Toast.LENGTH_SHORT).show();
        new Thread(()->{
            try {
                GoogleDriveHelper.uploadBackup(getContext()).join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            SaveFragment.this.getActivity().runOnUiThread(()-> {
                Toast.makeText(getContext(), "Finished backup upload", Toast.LENGTH_LONG).show();
                SharedPreferences sp = getContext().getSharedPreferences("info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.putLong("last uploaded", System.currentTimeMillis());

                editor.apply();
            });

        }).start();
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
        if (requestCode == GOOGLE_SIGNIN && resultCode == Activity.RESULT_OK)
            modifyGoogleBtn(getActivity().findViewById(R.id.accountsGoogleBtn));



    }
}