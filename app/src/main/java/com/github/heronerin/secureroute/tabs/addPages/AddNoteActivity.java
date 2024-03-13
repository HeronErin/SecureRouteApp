package com.github.heronerin.secureroute.tabs.addPages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.github.heronerin.secureroute.CameraManager;
import com.github.heronerin.secureroute.R;
@SuppressLint("MissingInflatedId")
public class AddNoteActivity extends Fragment {
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
    CameraManager cameraManager;
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
        cameraManager = new CameraManager(v.findViewById(R.id.addImg), getActivity());
//        et.setOnEditorActionListener((TextView.OnEditorActionListener) (v1, actionId, event) -> {

//        });



        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        cameraManager.onActivityResult(requestCode, resultCode, data);
    }
}