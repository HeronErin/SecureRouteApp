package com.github.heronerin.secureroute.tabs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
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

    private LineData genGross(){
        DataBase db = DataBase.getOrCreate(getContext());

        List<Entry> entryList = new ArrayList<>();

        LineDataSet dataSet = new LineDataSet(entryList, "Gross income over time");



        return new LineData(dataSet);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats, container, false);
        LineChart chart = v.findViewById(R.id.chart);
        chart.setData(genGross());
        chart.getDescription().setEnabled(false);
        chart.getLegend();

        return v;
    }
}