package com.github.heronerin.secureroute.eventViewer;

import static com.github.heronerin.secureroute.eventViewer.NoteViewer.handleNoteViewAndImgs;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.TripUtils;
import com.github.heronerin.secureroute.events.Event;

import java.util.Calendar;
import java.util.List;

public class GasRangeViewer extends AppCompatActivity {
//    public static final double mileDeduction = 0.67D;
    public static double mileDeduction(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        if (c.get(Calendar.YEAR) == 2023)
            return 65.5D / 100D;
        return 67D / 100D;
    }

    Event event;
    Event endEvent = null;
    Event eventFollowing = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gas_range_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        assert extras != null;

        event = Event.decodeFromString(extras.getString("event"));

        TextView spend = findViewById(R.id.spendAmount);
        TextView tripInfo = findViewById(R.id.tripInfo);
        String info = "Starting odometer: " + event.odometer;

        spend.setText("Amount spend: $" + event.moneyAmount);
        spend.setBackgroundColor(0xFFFFD700);
        long end = Long.MAX_VALUE;
        if (event.associatedPair == -1)
            info+="\nStill ongoing";
        else{
            endEvent = DataBase.getOrCreate(this).getEventById(event.associatedPair);
            if (endEvent != null) {
                eventFollowing = DataBase.instance.getEventAfterTimeOfType(Event.EventVariety.GasEvent, endEvent.timeStamp);

                info += "\nEnding odometer: " + endEvent.odometer;
                info += "\nMiles traveled: " + (endEvent.odometer - event.odometer);
                info += "\nTime between fillups: " + TripUtils.formatMillisecondsToTime(endEvent.timeStamp - event.timeStamp);
                end = endEvent.timeStamp;
            }
        }
        List<Event> containing = DataBase.getOrCreate(this).getInTimeFrame(event.timeStamp, end);
        event.cachedRanges = containing;
        double businessMiles = 0;
        for (Event event2 : containing){
            if (event2.variety != Event.EventVariety.TripEnd) continue;

            Event event3 = DataBase.instance.getEventById(event2.associatedPair);

            if (event3 == null) continue;
            if (event3.timeStamp > end || event3.timeStamp < event.timeStamp) continue;

            businessMiles+=event2.odometer-event3.odometer;
        }
        for (Event event2 : containing){
            if (event2.variety != Event.EventVariety.FullTrip) continue;
            businessMiles+=event2.sumFullTripMiles();
        }
        info+="\nBusiness Miles: " + businessMiles;
        info+="\nBusiness Mile Deductions: " + (businessMiles * mileDeduction(event.timeStamp));
        Double buisnessPercent = null;
        if (endEvent != null)
            info+="\n\nBusiness usage: " +((buisnessPercent = (businessMiles / Double.valueOf(endEvent.odometer - event.odometer))) * 100) + "%";
        if (eventFollowing != null){
            info += "\nGas event cost following: " + eventFollowing.moneyAmount;
            if (buisnessPercent != null)
                info += "\nNext gas event deduction: " + (eventFollowing.moneyAmount * buisnessPercent);
        }

        tripInfo.setText(info);


        handleNoteViewAndImgs(event, this);
    }
}