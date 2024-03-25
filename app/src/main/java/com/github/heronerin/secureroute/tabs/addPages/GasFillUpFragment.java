package com.github.heronerin.secureroute.tabs.addPages;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.ImageManager;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;


public class GasFillUpFragment extends AbstractAddPage {
    public GasFillUpFragment() {
        // Required empty public constructor
    }
    SharedPreferences sharedPreferences;
    public static GasFillUpFragment newInstance() {
        GasFillUpFragment fragment = new GasFillUpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_gas_fill_up, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);

        final EditText noteField = v.findViewById(R.id.noteField);
        final EditText odometer = v.findViewById(R.id.odometer);
        final EditText gasFillupCost = v.findViewById(R.id.gasFillupCost);


        Bundle args = this.getArguments();
        if (args == null) {
            noteField.setText(sharedPreferences.getString("noteBox", ""));
            odometer.setText(sharedPreferences.getString("odometer", ""));
            gasFillupCost.setText(sharedPreferences.getString("gasFillupCost", ""));
        }else{
            throw new RuntimeException();
        }
        View.OnFocusChangeListener focusChangeListener = (v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", noteField.getText().toString());
            e.putString("odometer", odometer.getText().toString());
            e.putString("gasFillupCost", gasFillupCost.getText().toString());

            e.apply();
        };
        noteField.setOnFocusChangeListener(focusChangeListener);


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
        if (jso != null)
            e.putString("lastImg", jso);
        e.apply();
    }

    @Override
    public boolean isValid() {
        String odometerText = ((EditText)getActivity().findViewById(R.id.odometer)).getText().toString();
        return !odometerText.isEmpty()
                && (
                        DataBase.instance.getLastWithOdometer() == null || Long.valueOf( odometerText ) >= DataBase.instance.getLastWithOdometer().odometer
                );
    }

    boolean usingImages = false;

    @Override
    public Event genValidEvent() {
        JSONArray jsonArray = new JSONArray();
        if (usingImages){
            try {
                jsonArray = new JSONArray(sharedPreferences.getString("lastImg", "[]"));
            } catch (JSONException e) { }
        }


        return new Event(
                Event.EventVariety.GasEvent,
                UUID.randomUUID(),
                System.currentTimeMillis(),
                Double.valueOf( ((EditText)getActivity().findViewById(R.id.gasFillupCost)).getText().toString() ),
                -1,
                ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString(),
                jsonArray,
                Long.valueOf( ((EditText)getActivity().findViewById(R.id.odometer)).getText().toString() )
        );
    }

    @Override
    public void clearStorage() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("noteBox", "");
        e.putString("gasFillupCost", "");
        if (usingImages){
            e.putString("lastImg", "[]");
            usingImages=false;
        }
        e.apply();
        ((EditText)getActivity().findViewById(R.id.noteField)).setText("");
        ((EditText)getActivity().findViewById(R.id.gasFillupCost)).setText("");
    }

    @Override
    public void setMode(int index) { }

    @Override
    public String getDisplay() {
        return "Gas FillUp";
    }

    @Override
    public String[] getSubTypes() {
        return null;
    }

}