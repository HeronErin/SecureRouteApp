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
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.ImageManager;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

@SuppressLint("MissingInflatedId")
public class AddIncomeFragment extends AbstractAddPage {

    @Override
    public boolean isValid() {
        return !isEmptyTextView(getActivity().findViewById(R.id.noteField));
    }
    @Override
    public void clearStorage() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("noteBox", "");
        e.putString("addAmount", "");
        if (usingImages){
            e.putString("lastImg", "[]");
            usingImages = false;
        }
        e.apply();
    }

    int isIncome = 0;
    @Override
    public void setMode(int index) { isIncome = index; }

    @Override
    public String getDisplay() {
        return "Add Income / Expense";
    }

    @Override
    public String[] getSubTypes() {
        return new String[]{"Income", "NonJob Expense", "Job Expense"};
    }
    Event.EventVariety eventVariety(){
        if (isIncome==0)
            return Event.EventVariety.Income;
        if (isIncome==1)
            return Event.EventVariety.Expense;
        if (isIncome==2)
            return Event.EventVariety.JobExpense;
        throw new RuntimeException("isIncome set incorrectly");
    }

    @Override
    public Event genValidEvent() {
        JSONArray jsonArray = new JSONArray();
        if (usingImages){
            try {
                jsonArray = new JSONArray(sharedPreferences.getString("lastImg", "[]"));
            } catch (JSONException e) { }
        }
        EditText amountField = getActivity().findViewById(R.id.addAmount);


        return new Event(
                eventVariety(),
                UUID.randomUUID(),
                System.currentTimeMillis(),
                Double.valueOf(amountField.getText().toString()),
                -1,
                ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString(),
                jsonArray,
                null
        );


    }

    public AddIncomeFragment() {
        // Required empty public constructor
    }
    public static AddIncomeFragment newInstance() {
        return new AddIncomeFragment();
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
        View v =  inflater.inflate(R.layout.fragment_add_income, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);


        final EditText noteField = v.findViewById(R.id.noteField);
        final EditText amountField = v.findViewById(R.id.addAmount);


        View.OnFocusChangeListener focus = (v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", noteField.getText().toString());
            e.putString("addAmount", amountField.getText().toString());
            e.apply();
        };
        Bundle args = this.getArguments();
        if (args == null) {
            amountField.setText(sharedPreferences.getString("addAmount", ""));

            noteField.setText(sharedPreferences.getString("noteBox", ""));
        }else{
            throw new RuntimeException();
        }
        noteField.setOnFocusChangeListener(focus);


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



}