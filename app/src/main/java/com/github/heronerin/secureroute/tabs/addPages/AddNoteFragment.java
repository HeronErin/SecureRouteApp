package com.github.heronerin.secureroute.tabs.addPages;
/**
 * Just a simple way to add a note to your timeline...
 * Use this as a template for future addPage items...
 */


import static android.app.Activity.RESULT_OK;

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

import com.github.heronerin.secureroute.ImageManager;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

public class AddNoteFragment extends AbstractAddPage {
    @Override
    public boolean isValid() {
        return usingImages || !((EditText) getActivity().findViewById(R.id.noteField)).getText().toString().trim().isEmpty();
    }
    @Override
    public void clearStorage() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("noteBox", "");
        if (usingImages){
            e.putString("lastImg", "[]");
            usingImages=false;
        }
        e.apply();
        ((EditText)getActivity().findViewById(R.id.noteField)).setText("");
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
            try {
                jsonArray = new JSONArray(sharedPreferences.getString("lastImg", "[]"));
            } catch (JSONException e) { }
        }


        return new Event(
                Event.EventVariety.ArbitraryNote,
                UUID.randomUUID(),
                System.currentTimeMillis(),
                0,
                -1,
                ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString(),
                jsonArray,
                null
        );
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_add_note_activity, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);

        EditText et = v.findViewById(R.id.noteField);


        String note = sharedPreferences.getString("noteBox", "");

        et.setText(note);

        et.setOnFocusChangeListener((v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", ((EditText) v1).getText().toString());
            e.apply();
        });

        v.findViewById(R.id.addImg).setOnClickListener((view)-> {
            usingImages=true;
            Intent intent = new Intent(this.getContext(), ImageManager.class);
            intent.putExtra("json", sharedPreferences.getString("lastImg", "[]"));
            startActivityForResult(intent, 69);
        });



        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != 69 || resultCode != RESULT_OK || data == null) return;

        SharedPreferences.Editor e = sharedPreferences.edit();
        String jso = data.getStringExtra("json");
        e.putString("lastImg", jso);
        e.apply();
    }


}