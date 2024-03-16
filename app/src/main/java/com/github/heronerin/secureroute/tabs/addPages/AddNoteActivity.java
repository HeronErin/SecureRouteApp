package com.github.heronerin.secureroute.tabs.addPages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.heronerin.secureroute.CameraManager;
import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.interactions.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@SuppressLint("MissingInflatedId")
public class AddNoteActivity extends AbstractAddPage {
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
            Log.d(getTag(), "Removing" + path);
            Log.d(getTag(), "Exists:" + f.exists());
            if (f.exists()) f.delete();
            usingImages = false;
        }
    }

    @Override
    public Event genValidEvent() {
        JSONArray jsonArray = new JSONArray();
        if (usingImages){
            StringBuilder sb = new StringBuilder();

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(CameraManager.instance.getTempPath()))) {

                int b;
                while (-1 != (b = inputStream.read())) {
                    sb.append((char) b);
                }

                jsonArray = (new JSONObject(sb.toString())).getJSONArray("imgs");
            }catch (FileNotFoundException e){
                Log.w(this.getTag(), e);
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }


        return new Event(
                Event.EventVariety.ArbitraryNote,
                UUID.randomUUID(),
                System.currentTimeMillis(),
                0,
                ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString(),
                jsonArray
        );
    }




    public AddNoteActivity() {
        // Required empty public constructor
    }
    public static AddNoteActivity newInstance() {
        AddNoteActivity fragment = new AddNoteActivity();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    SharedPreferences sharedPreferences;
    boolean usingImages = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_add_note_activity, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);
        String note = sharedPreferences.getString("noteBox", "");

        EditText et = v.findViewById(R.id.noteField);
        et.setText(note);
        et.setOnFocusChangeListener((v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", ((EditText) v1).getText().toString());
            e.apply();
        });
        assert CameraManager.instance != null;
        v.findViewById(R.id.addImg).setOnClickListener((view)->
            CameraManager.instance.openTemp(()->usingImages=true)
        );



        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    }


}