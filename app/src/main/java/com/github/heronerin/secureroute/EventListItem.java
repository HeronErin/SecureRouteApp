package com.github.heronerin.secureroute;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.heronerin.secureroute.interactions.Event;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventListItem#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventListItem extends Fragment {
    public Event event;
    public EventListItem() {
        // Required empty public constructor
    }

    public static EventListItem newInstance(Event event) {
        EventListItem fragment = new EventListItem();
        Bundle args = new Bundle();

//        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            event = getArguments().getSerializable("event", Event.class);
        }else{
            Toast.makeText(this.getContext(), "Please pass in params to EventListItem", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_list_item, container, false);
    }
}