package com.github.heronerin.secureroute.tabs;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StatsFragment extends Fragment {

    public StatsFragment() {
        // Required empty public constructor
    }


    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private float floatifyTime(long timestamp){
        return ((float)(timestamp/1000)) / 60f;
    }
    private long deFloatifyTime(float time){
        return (long)(time * 60f) * 1000L;
    }
    private void genIncome(LineChart chart){
        DataBase db = DataBase.getOrCreate(getContext());
        List<Entry> entryList = new ArrayList<>();

        // Starts with max
        List<Event> events = db.getEventsOfVariety(new Event.EventVariety[]{Event.EventVariety.Income}, Integer.MAX_VALUE);
        Collections.reverse(events);
        Long min = null;
        Float max = null;
        // Now starts with min
        for (Event incomeEvent : events){
            if (min == null)
                min = incomeEvent.timeStamp;
            float ftime = floatifyTime(incomeEvent.timeStamp - min);
            entryList.add(new Entry(ftime, (float) incomeEvent.moneyAmount));

            max = ftime;
            Log.d("Income", String.valueOf(ftime));
        }

        LineDataSet dataSet = new LineDataSet(entryList, "Money made over time");

        XAxis xAxis = chart.getXAxis();

        Long finalMin = min;
        xAxis.setValueFormatter((value, axis)->
                new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date(deFloatifyTime(value) + finalMin))
        );

        if (max != null) {
            xAxis.setGranularity(max / 4F);
            xAxis.setAxisMaximum(max * 1.2f);
        }

        xAxis.setAxisMinimum(0);


        chart.setData(new LineData(dataSet));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats, container, false);
        LineChart chart = v.findViewById(R.id.chart);
//        XAxis xAxis = chart.getXAxis();
//        xAxis.setValueFormatter((value ,ve)-> new SimpleDateFormat("MM/dd/yyyy").format(new Date((long) value)));
//        xAxis.setGranularity(60*60);
//        chart.setData(genIncome());
        genIncome(chart);
//        chart.getDescription().setEnabled(false);
//        chart.getLegend();
//        chart.invalidate();

        return v;
    }
}