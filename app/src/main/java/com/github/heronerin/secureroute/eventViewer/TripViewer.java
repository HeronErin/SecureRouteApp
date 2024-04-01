package com.github.heronerin.secureroute.eventViewer;

import static com.github.heronerin.secureroute.TripUtils.formatMillisecondsToTime;
import static com.github.heronerin.secureroute.eventViewer.NoteViewer.handleNoteViewAndImgs;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

public class TripViewer extends AppCompatActivity {
    Event event;

    Event startOfTrip = null;
    Event endOfTrip = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        assert extras != null;

        event = Event.decodeFromString(extras.getString("event"));
        handleNoteViewAndImgs(event, this);

        if (event.associatedPair == -1){
            ((TextView)findViewById(R.id.tripInfo)).setText("On going trip...\nPlease end this in the add menu\nS: " + event.odometer);
            findViewById(R.id.tripInfo).setBackgroundColor(Color.YELLOW);
            return;
        }
        if (event.variety == Event.EventVariety.TripStart) {
            endOfTrip = DataBase.getOrCreate(this).getEventById(event.associatedPair);
            startOfTrip = event;
        }
        else {
            startOfTrip = DataBase.getOrCreate(this).getEventById(event.associatedPair);
            endOfTrip = event;
        }
        TextView tripInfo = findViewById(R.id.tripInfo);
        String tripStats = "Miles driven: " + String.valueOf(endOfTrip.odometer - startOfTrip.odometer);
        tripStats+="\nTime taken: " + formatMillisecondsToTime(endOfTrip.timeStamp - startOfTrip.timeStamp);
        tripStats+="\n";
        tripStats+="\nStarting odometer: " + startOfTrip.odometer;
        tripStats+="\nEnding odometer: " + endOfTrip.odometer;

        tripInfo.setText(tripStats);
    }
}