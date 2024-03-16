package com.github.heronerin.secureroute.tabs.addPages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.heronerin.secureroute.CameraManager;
import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.interactions.Event;
import com.github.heronerin.secureroute.tabs.EventArrayAdapter;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RangeEventAddFragment extends AbstractAddPage {

    public RangeEventAddFragment() {
        // Required empty public constructor
    }


    public static RangeEventAddFragment newInstance() {
        RangeEventAddFragment fragment = new RangeEventAddFragment();

        return fragment;
    }
    SharedPreferences sharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);


    }
    ConstraintLayout EndOfRange = null;
    ConstraintLayout StartOfRange = null;
    boolean usingImages = false;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_range_event_add, container, false);
        EndOfRange = v.findViewById(R.id.endRangeSettings);
        StartOfRange = v.findViewById(R.id.startRangeSettings);

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
    public boolean isValid() {
        if (currentMode == 0)
            return usingImages || !((EditText) getActivity().findViewById(R.id.noteField)).getText().toString().trim().isEmpty();
        if (currentMode == 1){
            Spinner endOfSpinner = EndOfRange.findViewById(R.id.endOfSpinner);
            return endOfSpinner.getSelectedItemPosition() != 0;
        }
        return false;

    }

    @Override
    public Event genValidEvent() {
        if (currentMode == 0) {
            JSONArray jsonArray = new JSONArray();
            if (usingImages){
                jsonArray = CameraManager.instance.getTempJsonArray();
            }

            return new Event(
                    Event.EventVariety.ArbitraryRangeStart,
                    UUID.randomUUID(),
                    System.currentTimeMillis(),
                    0, -1,
                    ((EditText) getActivity().findViewById(R.id.noteField)).getText().toString(),
                    jsonArray
                    );
        }
        if (currentMode == 1){
            Spinner endOfSpinner = EndOfRange.findViewById(R.id.endOfSpinner);
            Event startEvent = unendedEvents.get(endOfSpinner.getSelectedItemPosition() - 1);

            return new Event(
                    Event.EventVariety.ArbitraryRangeEnd,
                    UUID.randomUUID(),
                    System.currentTimeMillis(),
                    0, startEvent.databaseId,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void clearStorage() {
        if (currentMode == 0) {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", "");
            e.apply();
            ((EditText) getActivity().findViewById(R.id.noteField)).setText("");
            if (usingImages) {
                String path = CameraManager.instance.getTempPath();
                if (path.startsWith("file:/")) path = path.substring("file:/".length());

                File f = new File(path);
                if (f.exists()) f.delete();
                usingImages = false;
            }
        }
    }
    List<Event> unendedEvents = null;

    int currentMode = 0;

    @Override
    public void setMode(int index) {
        currentMode = index;
        if (StartOfRange == null || EndOfRange == null) return;
        (new View[]{StartOfRange, EndOfRange})[index].setVisibility(View.VISIBLE);
        (new View[]{StartOfRange, EndOfRange})[(index+1)%2].setVisibility(View.GONE);
        if (currentMode == 1){
            unendedEvents = DataBase.instance.getUnEndedRanges(Integer.MAX_VALUE, true);
            List<String> displayNames = new ArrayList<>();
            displayNames.add("Select A Range to end");
            for (Event event : unendedEvents)
                displayNames.add(EventArrayAdapter.noteDataHandle(event.noteData));

            Spinner endOfSpinner = EndOfRange.findViewById(R.id.endOfSpinner);
            endOfSpinner.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, displayNames));
        }
    }
}