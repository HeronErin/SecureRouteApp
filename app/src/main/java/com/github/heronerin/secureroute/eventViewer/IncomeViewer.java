package com.github.heronerin.secureroute.eventViewer;

import static com.github.heronerin.secureroute.eventViewer.NoteViewer.handleNoteViewAndImgs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;

public class IncomeViewer extends AppCompatActivity {
    Event event;
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.income_viewer);

        EdgeToEdge.enable(this);


        Bundle extras = getIntent().getExtras();
        assert extras != null;

        event = Event.decodeFromString(extras.getString("event"));
        handleNoteViewAndImgs(event, this);
        TextView amount = findViewById(R.id.spendAmount);
        if (event.variety == Event.EventVariety.Income){
            amount.setTextColor(Color.GREEN);
            amount.setText("$"+event.moneyAmount);
        }
        if (event.variety == Event.EventVariety.Income){
            amount.setTextColor(Color.GREEN);
            amount.setText("$"+event.moneyAmount);
        }
        if (event.variety == Event.EventVariety.JobExpense){
            amount.setTextColor(Color.RED);
            amount.setText("$"+event.moneyAmount +" spent on the job");
        }
        if (event.variety == Event.EventVariety.Expense){
            amount.setTextColor(0xFFFFD700);
            amount.setText("$"+event.moneyAmount +" spent");
        }

    }
}
