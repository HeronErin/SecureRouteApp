package com.github.heronerin.secureroute.eventViewer;

import static com.github.heronerin.secureroute.eventViewer.NoteViewer.handleNoteViewAndImgs;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.tabs.addPages.AddMillageTrackingFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FullTripViewer extends AppCompatActivity {
    Event event;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.income_viewer);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        assert extras != null;

        event = Event.decodeFromString(extras.getString("event"));
        handleNoteViewAndImgs(event, this);




        ((TextView)findViewById(R.id.spendAmount)).setText("Miles driven: " + event.sumFullTripMiles());



        LinearLayout ll = findViewById(R.id.viewerList);

        ListView listView = new ListView(this);

        AddMillageTrackingFragment.RemovableStringList removableStringList = null;

        try {
            List<String> mileList = new ArrayList<>();

            JSONObject data = new JSONObject(event.noteData);
            JSONArray miles = data.getJSONArray("miles");
            for(int i = 0; i < miles.length(); i++){
                mileList.add(miles.getString(i));
            }
            listView.setAdapter(removableStringList = new AddMillageTrackingFragment.RemovableStringList(this, mileList, false));
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ll.addView(listView, 3);

        listView.getLayoutParams().height = displayMetrics.heightPixels / 3;
        listView.getLayoutParams().width =  displayMetrics.widthPixels - 50;
        listView.setPadding(25, 0, 0, 0);
    }
}