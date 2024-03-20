package com.github.heronerin.secureroute.tabs.addPages;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.heronerin.secureroute.CameraManager;
import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;

import java.io.IOException;
import java.util.UUID;


public class GasFillUpFragment extends AbstractAddPage {
    public GasFillUpFragment() {
        // Required empty public constructor
    }
    SharedPreferences sharedPreferences;
    Event revise;
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
            revise = Event.decodeFromString(args.getString("event"));
            v.findViewById(R.id.addImg).setVisibility(View.GONE);
            try {
                CameraManager.instance.putTempJsonArray(revise.getImageData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            noteField.setText(revise.noteData);
        }
        View.OnFocusChangeListener focusChangeListener = (v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", noteField.getText().toString());
            e.putString("odometer", odometer.getText().toString());
            e.putString("gasFillupCost", gasFillupCost.getText().toString());

            e.apply();
        };
        noteField.setOnFocusChangeListener(focusChangeListener);

        assert CameraManager.instance != null;
        v.findViewById(R.id.addImg).setOnClickListener((view)-> {
            usingImages=true;
            CameraManager.instance.openTemp(() -> usingImages = true);
        });

        return v;
    }

    @Override
    public boolean isValid() {
        String odometerText = ((EditText)getActivity().findViewById(R.id.odometer)).getText().toString();
        return !odometerText.isEmpty()
                && (
                        revise != null || DataBase.instance.getLastWithOdometer() == null || Long.valueOf( odometerText ) >= DataBase.instance.getLastWithOdometer().odometer
                );
    }

    boolean usingImages = false;

    @Override
    public Event genValidEvent() {
        JSONArray jsonArray = new JSONArray();
        if (usingImages){
            jsonArray = CameraManager.instance.getTempJsonArray();
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