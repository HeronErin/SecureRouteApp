package com.github.heronerin.secureroute.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.EventEditUtils;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    SharedPreferences settings;
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void save(View ignored){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("godTransmute", doGodTransmute.isChecked());
        for (Pair<CheckBox, String> boxStringPair : checksBoxes){
            editor.putBoolean(boxStringPair.second, boxStringPair.first.isChecked());
        }
        editor.putInt("uploadInt", uploadInterval.getSelectedItemPosition());
        editor.putString("retentionAmount", retentionAmount.getText().toString());

        editor.apply();
    }
    CheckBox doGodTransmute;
    Spinner uploadInterval;
    EditText retentionAmount;
    public List<Pair<CheckBox, String>> checksBoxes = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        checksBoxes.clear();
        checksBoxes.add(
                new Pair<>(
                        v.findViewById(R.id.autoUpload),
                        "autoUpload"
                )
        );
        checksBoxes.add(
                new Pair<>(
                        v.findViewById(R.id.autoUploadOnData),
                        "autoUploadOnData"
                )
        );
        checksBoxes.add(
                new Pair<>(
                        v.findViewById(R.id.autoPrune),
                        "autoPrune"
                )
        );
        checksBoxes.add(
                new Pair<>(
                        v.findViewById(R.id.hasRangeEnds),
                        "hasRangeEnds"
                )
        );

        for (Pair<CheckBox, String> boxStringPair : checksBoxes){
            boxStringPair.first.setChecked(settings.getBoolean(boxStringPair.second, false));
            boxStringPair.first.setOnCheckedChangeListener((buttonView, isChecked) -> save(buttonView));
        }

        doGodTransmute = v.findViewById(R.id.doGodTransmute);

        doGodTransmute.setChecked(settings.getBoolean("godTransmute", false));
        doGodTransmute.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                EventEditUtils.confirm(
                        () -> save(buttonView),
                        ()-> buttonView.setChecked(false),
                        "God transmutation is dangerous.",
                        "Using god transmutation can destroy your data! Use at your own risk!",
                        SettingsFragment.this.getContext());
            }else {
                save(buttonView);
            }
        });


        // Upload interval (see uploadIntervalToTimeDiff() )
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{
                "After every upload",
                "The hour of an upload",
                "The half-day of an upload",
                "The day of an upload",
                "The half-week of an upload",
                "The week of an upload"
        });
        uploadInterval = v.findViewById(R.id.timeToUpload);
        uploadInterval.setAdapter(stringArrayAdapter);
        uploadInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                save(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        uploadInterval.setSelection(settings.getInt("uploadInt", 0));


        retentionAmount = v.findViewById(R.id.amountOfUploadsToKeep);
        retentionAmount.setText(settings.getString("retentionAmount", ""));
        retentionAmount.setOnFocusChangeListener((v1, hasFocus) -> save(v1));




        return v;
    }

    public static long uploadIntervalToTimeDiff(int interval){
        switch (interval){
            case 0:
                return 0;
            case 1:
                return 1000*60*60;
            case 2:
                return 1000*60*60*12;
            case 3:
                return 1000*60*60*24;
            case 4:
                return 1000*60*60*12*3;
            case 5:
                return 1000*60*60*12*7;
        }
    }
}