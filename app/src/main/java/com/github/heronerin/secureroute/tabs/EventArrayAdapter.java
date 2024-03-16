package com.github.heronerin.secureroute.tabs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.ImageViewerFragment;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.interactions.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private Context mContext;
    private List<Event> moviesList = new ArrayList<>();

    public EventArrayAdapter(@NonNull Context context, List<Event> list) {
        super(context, 0 , list);
        mContext = context;
        moviesList = list;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.event_list_item,parent,false);

        return listItem;
    }
}