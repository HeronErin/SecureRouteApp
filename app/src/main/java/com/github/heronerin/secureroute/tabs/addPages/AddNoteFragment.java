package com.github.heronerin.secureroute.tabs.addPages;
/**
 * Just a simple way to add a note to your timeline...
 * Use this as a template for future addPage items...
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.CameraManager;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@SuppressLint("MissingInflatedId")
public class AddNoteFragment extends AbstractAddPage {
    @Override
    public boolean isValid() {
        return usingImages || !((EditText) getActivity().findViewById(R.id.noteField)).getText().toString().trim().isEmpty();
    }
    @Override
    public void clearStorage() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("noteBox", "");
        e.apply();
        ((EditText)getActivity().findViewById(R.id.noteField)).setText("");
        if (usingImages){
            String path = CameraManager.instance.getTempPath();
            if (path.startsWith("file:/")) path=path.substring("file:/".length());

            File f = new File(path);
            if (f.exists()) f.delete();
            usingImages = false;
        }
    }

    @Override
    public void setMode(int index) { }

    @Override
    public String getDisplay() {
        return "Add note";
    }

    @Override
    public String[] getSubTypes() {
        return null;
    }

    @Override
    public Event genValidEvent() {
        JSONArray jsonArray = new JSONArray();
        if (usingImages){
            jsonArray = CameraManager.instance.getTempJsonArray();
        }

        if (revise == null)
            return new Event(
                    Event.EventVariety.ArbitraryNote,
                    UUID.randomUUID(),
                    System.currentTimeMillis(),
                    0,
                    -1,
                    ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString(),
                    jsonArray
            );
        revise.noteData =  ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString();
        revise.setImageData(jsonArray);
        return revise;
    }

    public AddNoteFragment() {
        // Required empty public constructor
    }
    public static AddNoteFragment newInstance() {
        AddNoteFragment fragment = new AddNoteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    SharedPreferences sharedPreferences;
    boolean usingImages = false;

    Event revise = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_add_note_activity, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);

        EditText et = v.findViewById(R.id.noteField);


        String note = sharedPreferences.getString("noteBox", "");

        Bundle args = this.getArguments();
        if (args == null)
            et.setText(note);
        else{
            revise = Event.decodeFromString(args.getString("event"));
            try {
                CameraManager.instance.putTempJsonArray(revise.getImageData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            et.setText(revise.noteData);
        }
        et.setOnFocusChangeListener((v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", ((EditText) v1).getText().toString());
            e.apply();
        });
        assert CameraManager.instance != null;
        v.findViewById(R.id.addImg).setOnClickListener((view)-> {
            usingImages=true;
            CameraManager.instance.openTemp(() -> usingImages = true);
        });



        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    }


}