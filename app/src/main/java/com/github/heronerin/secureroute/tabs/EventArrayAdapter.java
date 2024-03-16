package com.github.heronerin.secureroute.tabs;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.interactions.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

    }
    public static String noteDataHandle(@Nullable String input){
        if (input == null) input = "";
        input=input.trim();

        String data = "Empty note";
        if (!input.isEmpty()){
            data = input;
            int nextLine = data.indexOf("\n");
            if (nextLine != -1 || data.length() > MAX_PREVIEW_SIZE){
                if (nextLine != -1) data = data.substring(0, nextLine) + "...";
                if (data.length() > MAX_PREVIEW_SIZE) data = data.substring(0, MAX_PREVIEW_SIZE) + "...";
            }
        }
        return data;
    }
    private static int[] bars = new int[]{R.id.redBar, R.id.blueBar, R.id.purpleBar, R.id.orangeBar, R.id.greenBar};
    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.w("this.getContext", String.valueOf(position));
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.event_list_item,parent,false);
        Event event = eventList.get(position);



        for (int i = 0; i < 5; i++){
            if (event.rangeCache[i])
                listItem.findViewById(bars[i]).setVisibility(View.VISIBLE);
        }


        ImageView imageView = listItem.findViewById(R.id.typeIcon);
        TextView dateTimeText = listItem.findViewById(R.id.dateTimeText);

        Date date = new Date(event.timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        dateTimeText.setText(formatter.format(date));

        TextView textPreview = listItem.findViewById(R.id.textPreview);
        if (event.variety == Event.EventVariety.ArbitraryNote){
            textPreview.setText(noteDataHandle(event.noteData));
            imageView.setImageResource(R.drawable.note_icon);
        }
        if (event.variety == Event.EventVariety.ArbitraryRangeStart){
            textPreview.setText(noteDataHandle(event.noteData));
            imageView.setImageResource(R.drawable.calender_icon);
        }


        return listItem;
    }
}