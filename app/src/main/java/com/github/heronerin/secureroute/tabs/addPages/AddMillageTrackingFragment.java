package com.github.heronerin.secureroute.tabs.addPages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

import java.util.List;

public class AddMillageTrackingFragment extends AbstractAddPage {
    public static class RemovableStringList extends ArrayAdapter<String>{

        List<String> strings;
        public RemovableStringList(@NonNull Context context, List<String> list) {
            super(context, 0 , list);
            strings = list;
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
            listItem.findViewById(R.id.trashBtn).setOnClickListener((__)-> this.remove(item));
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_millage_track, container, false);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Event genValidEvent() {
        return null;
    }

    @Override
    public void clearStorage() {

    }

    @Override
    public void setMode(int index) { }

    @Override
    public String getDisplay() { return "Add previous miles"; }

    @Override
    public String[] getSubTypes() { return null; }
}