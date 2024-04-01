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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.ImageManager;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddMillageTrackingFragment extends AbstractAddPage {
    public static class RemovableStringList extends ArrayAdapter<String>{

        List<String> strings;
        boolean canRemove;
        public RemovableStringList(@NonNull Context context, List<String> list, boolean canRemove) {
            super(context, 0 , list);
            strings = list;
            this.canRemove=canRemove;
        }

        @SuppressLint("ResourceType")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(getContext()).inflate(R.layout.backup_list_item, parent, false);

            String item = strings.get(position);

            ((TextView)listItem.findViewById(R.id.backupDate)).setText(item);
            if (canRemove)
                listItem.findViewById(R.id.trashBtn).setOnClickListener((__)-> this.remove(item));
            else
                listItem.findViewById(R.id.trashBtn).setVisibility(View.GONE);

            listItem.findViewById(R.id.downloadBtn).setVisibility(View.GONE);
            return listItem;
        }
    }

    public AddMillageTrackingFragment() {
        // Required empty public constructor
    }

    public static AddMillageTrackingFragment newInstance() {
        AddMillageTrackingFragment fragment = new AddMillageTrackingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    RemovableStringList millageList;
    SharedPreferences sharedPreferences;
    boolean usingImages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_millage_track, container, false);

        sharedPreferences = getActivity().getSharedPreferences("AutoSave", Context.MODE_PRIVATE);

        List<String> miles = new ArrayList<>();
        try{
            JSONArray oldMiles = new JSONArray(sharedPreferences.getString("miles", "[]"));
            for (int i = 0; i < oldMiles.length(); i++){
                miles.add(oldMiles.getString(i));
            }
        }catch (JSONException ignored) {}

        millageList = new RemovableStringList(getContext(), miles, true);
        ((ListView) v.findViewById(R.id.mileList)).setAdapter(millageList);




        EditText et = v.findViewById(R.id.noteField);
        et.setText(sharedPreferences.getString("noteBox", ""));

        et.setOnFocusChangeListener((v1, hasFocus) -> {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("noteBox", ((EditText) v1).getText().toString());
            e.apply();
        });

        v.findViewById(R.id.addMile).setOnClickListener((vv)->{
            EditText mileEnter = v.findViewById(R.id.mileEnter);
            if (mileEnter.getText().length() == 0) return;

            millageList.add(mileEnter.getText().toString());

            JSONArray saveTo = new JSONArray();
            for (String string : millageList.strings){
                saveTo.put(string);
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("miles", saveTo.toString());
            e.apply();
            mileEnter.setText("");
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
    public boolean isValid() {
        return !millageList.isEmpty();
    }

    @Override
    public Event genValidEvent() {
        JSONArray jsonArray = new JSONArray();
        if (usingImages){
            try {
                jsonArray = new JSONArray(sharedPreferences.getString("lastImg", "[]"));
            } catch (JSONException e) { }
        }
        JSONObject storage = new JSONObject();

        try {
            storage.put("note", ((EditText)getActivity().findViewById(R.id.noteField)).getText().toString());
            JSONArray miles = new JSONArray();
            for (String string : millageList.strings){
                miles.put(string);
            }
            storage.put("miles", miles);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new Event(
                Event.EventVariety.FullTrip,
                UUID.randomUUID(),
                System.currentTimeMillis(),
                0,
                -1,
                storage.toString(),
                jsonArray,
                null
        );
    }

    @Override
    public void clearStorage() {
        millageList.clear();
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("noteBox", "");
        e.putString("miles", "[]");
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
    public String getDisplay() { return "Add previous miles"; }

    @Override
    public String[] getSubTypes() { return null; }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != 69 || resultCode != RESULT_OK || data == null) return;

        SharedPreferences.Editor e = sharedPreferences.edit();
        String jso = data.getStringExtra("json");
        e.putString("lastImg", jso);
        e.apply();
    }
}