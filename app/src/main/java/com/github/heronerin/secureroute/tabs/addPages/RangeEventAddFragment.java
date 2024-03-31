package com.github.heronerin.secureroute.tabs.addPages;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.ImageManager;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

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

        v.findViewById(R.id.addImg).setOnClickListener((view)->{
            usingImages=true;
            Intent intent = new Intent(this.getContext(), ImageManager.class);
            intent.putExtra("json", sharedPreferences.getString("lastImg", "[]"));
            startActivityForResult(intent, 69);
        });


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
                try {
                    jsonArray = new JSONArray(sharedPreferences.getString("lastImg", "[]"));
                } catch (JSONException e) { }
            }

            return new Event(
                    Event.EventVariety.ArbitraryRangeStart,
                    UUID.randomUUID(),
                    System.currentTimeMillis(),
                    0, -1,
                    ((EditText) getActivity().findViewById(R.id.noteField)).getText().toString(),
                    jsonArray,
                    null
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
            if (usingImages) {
                e.putString("lastImg", "[]");
                usingImages = false;
            }
            e.apply();
            ((EditText) getActivity().findViewById(R.id.noteField)).setText("");
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
                displayNames.add(event.eventPreview());

            Spinner endOfSpinner = EndOfRange.findViewById(R.id.endOfSpinner);
            endOfSpinner.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, displayNames));
        }
    }

    @Override
    public String getDisplay() {
        return "Range";
    }

    @Override
    public String[] getSubTypes() {
        return new String[]{"Start", "End"};
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

}