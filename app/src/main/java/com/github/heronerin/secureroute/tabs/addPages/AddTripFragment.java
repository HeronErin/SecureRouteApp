package com.github.heronerin.secureroute.tabs.addPages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.github.heronerin.secureroute.CameraManager;
import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AddTripFragment extends AbstractAddPage {
    @Override
    public boolean isValid() {
        String odometerText = ((EditText)getActivity().findViewById(R.id.odometerV)).getText().toString();
        return !odometerText.isEmpty()
                && (
                revise != null || DataBase.instance.getLastWithOdometer() == null || Long.valueOf( odometerText ) >= DataBase.instance.getLastWithOdometer().odometer
        );
    }

    @Override
    public Event genValidEvent() {
        return new Event(
                isEnding ? Event.EventVariety.TripEnd : Event.EventVariety.TripStart,
                UUID.randomUUID(),
                System.currentTimeMillis(),
                0,
                isEnding ? lastKnownEnd.databaseId : -1,
                ((EditText) getActivity().findViewById(R.id.noteField)).getText().toString(),
                new JSONArray(),
                Long.valueOf(((EditText) getActivity().findViewById(R.id.odometerV)).getText().toString())
        );
    }

    SharedPreferences sharedPreferences;
    Event revise = null;
    @Override
    public void clearStorage() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("noteBox", "");
        e.putString("odometer", "");
        e.apply();
        ((EditText) getActivity().findViewById(R.id.noteField)).setText("");

    }

    @Override
    public void setMode(int index) {
    }

    @Override
    public String getDisplay() {
//        DataBase.instance.
        return "Add Trip";
    }

    @Override
    public String[] getSubTypes() {
        return null;
    }
    AddTripFragment() {
        // Required empty public constructor
    }

    public static AddTripFragment newInstance() {
        return new AddTripFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean checkIsEnding(){
        if (lastKnownEnd == null)
            return false;
        if (lastKnownEnd.variety == Event.EventVariety.TripEnd)
            return false;
        return true;
    }
    Event lastKnownEnd;
    boolean isEnding;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v =  inflater.inflate(R.layout.fragment_add_trip, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);

        final EditText noteField = v.findViewById(R.id.noteField);
        final EditText odometer = v.findViewById(R.id.odometerV);

        lastKnownEnd = DataBase.getOrCreate(this.getContext()).getLastTrip();
        isEnding = checkIsEnding();


        Bundle args = this.getArguments();
        if (args == null) {
            noteField.setText(sharedPreferences.getString("noteBox", ""));
            odometer.setText(sharedPreferences.getString("odometer", ""));
        }else{
            revise = Event.decodeFromString(args.getString("event"));
            noteField.setText(revise.noteData);
            if (revise.odometer != null)
                odometer.setText(String.valueOf(revise.odometer));
        }

        if (!isEnding) {
            ((TextView) v.findViewById(R.id.TripStatus)).setText("Start a new trip");
            v.findViewById(R.id.TripStatus).setBackgroundColor(Color.GREEN);
        }else{
            ((TextView) v.findViewById(R.id.TripStatus)).setText("End the current trip");
            v.findViewById(R.id.TripStatus).setBackgroundColor(0xFFAA0000);
        }

        View.OnFocusChangeListener focusChangeListener = (v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", noteField.getText().toString());
            e.putString("odometer", odometer.getText().toString());

            e.apply();
        };
        noteField.setOnFocusChangeListener(focusChangeListener);
        odometer.setOnFocusChangeListener(focusChangeListener);


        return v;
    }


}