package com.github.heronerin.secureroute.eventViewer;

import static com.github.heronerin.secureroute.eventViewer.NoteViewer.handleNoteViewAndImgs;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.tabs.addPages.AddIncomeFragment;

public class GasRangeViewer extends AppCompatActivity {

    Event event;
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
        handleNoteViewAndImgs(event, this, AddIncomeFragment.class);
        TextView spend = findViewById(R.id.spendAmount);
        spend.setText("Amount spend: $" + event.moneyAmount);
        if (event.associatedPair == -1){

        }
    }
}