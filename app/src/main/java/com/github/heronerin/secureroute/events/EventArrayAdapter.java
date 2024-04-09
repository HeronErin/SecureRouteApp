package com.github.heronerin.secureroute.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {


    private List<Event> eventList = new ArrayList<>();

    private boolean hasMenu;
    public EventArrayAdapter(@NonNull Context context, List<Event> list, boolean hasMenu) {
        super(context, 0 , list);
        eventList = list;
        this.hasMenu=hasMenu;
    }


    private static int[] bars = new int[]{R.id.redBar, R.id.blueBar, R.id.purpleBar, R.id.orangeBar, R.id.greenBar};
    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item,parent,false);


        final Event event = eventList.get(position);

        for (int i = 0; i < 5; i++)
            listItem.findViewById(bars[i]).setVisibility(
                    event.rangeCache[i] ? View.VISIBLE : View.INVISIBLE
            );



        ImageView imageView = listItem.findViewById(R.id.typeIcon);
        TextView dateTimeText = listItem.findViewById(R.id.dateTimeText);

        Date date = new Date(event.timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        dateTimeText.setText(formatter.format(date));

        TextView textPreview = listItem.findViewById(R.id.textPreview);

        textPreview.setText(event.eventPreview());
        imageView.setImageResource(event.getIcon());

        listItem.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), event.getViewerClass());
            intent.putExtra("event", event.encodeAsString());
            getContext().startActivity(intent);
        });
        if (hasMenu)
            listItem.setOnCreateContextMenuListener((menu, v, menuInfo) -> event.handleContext(getContext(), menu));

        return listItem;
    }

}