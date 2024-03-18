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
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.events.EventArrayAdapter;

import java.util.ArrayList;
import java.util.List;

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

        DataBase db = DataBase.getOrCreate(this.getContext());

        List<Event> eventList = db.getEventsByTime(100, true);
        List<Event> rangeList = db.getRange(Integer.MAX_VALUE, true);

        Event.applyRanges(eventList, rangeList);

        List<Event> filteredEventList = new ArrayList<>();
        for (Event event : eventList)
            if (!Event.isRangeEnd(event.variety))
                filteredEventList.add(event);

        eventArrayAdapter = new EventArrayAdapter(this.getContext(),
                filteredEventList
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