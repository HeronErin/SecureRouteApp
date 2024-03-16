package com.github.heronerin.secureroute.tabs;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;

import java.util.ArrayList;

public class EventList extends Fragment {
    EventArrayAdapter eventArrayAdapter;
    public EventList() {
        // Required empty public constructor
    }

    public static EventList newInstance() {
        EventList fragment = new EventList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventArrayAdapter = new EventArrayAdapter(this.getContext(),
                DataBase.getOrCreate(this.getContext()).getEventsByTime(100, true)
        );
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View out =  inflater.inflate(R.layout.fragment_event_list, container, false);
        ListView listView = out.findViewById(R.id.eventListHolder);
        listView.setAdapter(eventArrayAdapter);
        return out;
    }

}