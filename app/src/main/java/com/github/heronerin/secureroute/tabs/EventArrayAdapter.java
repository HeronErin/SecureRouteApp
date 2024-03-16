package com.github.heronerin.secureroute.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.interactions.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    public static final int MAX_PREVIEW_SIZE = 128;

    private Context mContext;
    private List<Event> eventList = new ArrayList<>();

    public EventArrayAdapter(@NonNull Context context, List<Event> list) {
        super(context, 0 , list);
        mContext = context;
        eventList = list;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.event_list_item,parent,false);
        Event event = eventList.get(position);

        ImageView imageView = listItem.findViewById(R.id.typeIcon);
        TextView dateTimeText = listItem.findViewById(R.id.dateTimeText);

        Date date = new Date(event.timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        dateTimeText.setText(formatter.format(date));

        TextView textPreview = listItem.findViewById(R.id.textPreview);
        if (event.variety == Event.EventVariety.ArbitraryNote){
            String data = "Empty note";
            if (event.noteData != null){
                data = event.noteData;
                int nextLine = data.indexOf("\n");
                if (nextLine != -1 || data.length() > MAX_PREVIEW_SIZE){
                    if (nextLine != -1) data = data.substring(0, nextLine) + "...";
                    if (data.length() > MAX_PREVIEW_SIZE) data = data.substring(0, MAX_PREVIEW_SIZE) + "...";
                }
            }
            textPreview.setText(data);
            imageView.setImageResource(R.drawable.note_icon);
        }

        return listItem;
    }
}