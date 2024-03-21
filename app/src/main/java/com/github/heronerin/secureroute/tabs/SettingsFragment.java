package com.github.heronerin.secureroute.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.github.heronerin.secureroute.R;

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
        editor.putBoolean("showEnd", hasRangeEnds.isChecked());

        editor.apply();
    }
    CheckBox hasRangeEnds;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        
        hasRangeEnds = v.findViewById(R.id.hasRangeEnds);
        hasRangeEnds.setChecked(settings.getBoolean("showEnd", false));
        hasRangeEnds.setOnCheckedChangeListener((buttonView, isChecked) -> save(buttonView));


        return v;
    }
}