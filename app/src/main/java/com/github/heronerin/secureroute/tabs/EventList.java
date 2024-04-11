package com.github.heronerin.secureroute.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.events.EventArrayAdapter;
import com.github.heronerin.secureroute.events.EventDataMineUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private SharedPreferences settings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        DataBase db = DataBase.getOrCreate(this.getContext());

        List<Event> eventList = db.getEventsByTime(Integer.MAX_VALUE, true);
        Collections.sort(eventList, (o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp));

        EventDataMineUtils.makeRanges(eventList);

        List<Event> filteredEventList = new ArrayList<>();
        for (Event event : eventList)
            if (!event.isRangeEnd(event.variety) || settings.getBoolean("showEnd", false))
                filteredEventList.add(0, event);

        eventArrayAdapter = new EventArrayAdapter(
                this.getContext(),
                filteredEventList,
                true
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