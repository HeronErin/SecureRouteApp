package com.github.heronerin.secureroute.tabs;

import static com.github.heronerin.secureroute.events.EventDataMineUtils.mileDeduction;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.events.EventDataMineUtils;
import com.github.heronerin.secureroute.events.EventEditUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    private void setupMoneyGraph(LineChart chart){
        XAxis xAxis = chart.getXAxis();

        xAxis.setGranularityEnabled(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(floatifyTime(1000*60*60*24));

        xAxis.setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
    }
//    private void genIncome(LineChart chart, int color){
//        DataBase db = DataBase.getOrCreate(getContext());
//        List<Entry> entryList = new ArrayList<>();
//
//        // Starts with max
//        List<Event> events = db.getEventsOfVariety(new Event.EventVariety[]{Event.EventVariety.Income}, Integer.MAX_VALUE);
//        Collections.reverse(events);
//        Long min = null;
//        Float max = null;
//
//        double moneyMade = 0;
//        // Now starts with min
//        for (Event incomeEvent : events){
//            if (min == null)
//                min = incomeEvent.timeStamp;
//
//            float ftime = floatifyTime(incomeEvent.timeStamp - min);
//            entryList.add(new Entry(ftime, (float) (moneyMade+=incomeEvent.moneyAmount)));
//
//            max = ftime;
//        }
//
//        LineDataSet dataSet = new LineDataSet(entryList, "Money made over time");
//
//        XAxis xAxis = chart.getXAxis();
//
//        Long finalMin = min;
//        xAxis.setValueFormatter((value, axis)->
//                new SimpleDateFormat("HH:mm MM/dd/yyyy").format(new Date(deFloatifyTime(value) + finalMin))
//        );
//
//        if (max != null) {
//            xAxis.setAxisMaximum(max * 1.2f);
//        }
//
//        chart.setTextColor(color);
//        dataSet.setValueTextColor(color);
//        dataSet.setDrawCircles(false);
//
//        chart.setData(new LineData(dataSet));
//
//    }
    String format(long time){ return new SimpleDateFormat("HH:mm MM/dd/yyyy").format(new Date(time)); }
    void genRange(long start, long end){
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);

        EventDataMineUtils.DataMineResults dataMineResults = DataBase.getOrCreate(getContext())
                                                                .dataMineFormRange(start, end);

        String info = format(start) + " - " + format(end) + ":\n";
        info += "\nGross income: $" + df.format(dataMineResults.grossProfit);
        info += "\nNet income: $" + df.format(dataMineResults.netProfit);
        info += "\nTotal expenses: $" + df.format(dataMineResults.totalExpenses);
        info += "\nMoney spent on gas: $" + df.format(dataMineResults.moneySpentOnGas);
        info += "\nTotal Trips: " + dataMineResults.totalTrips;
        info += "\nTotal Miles driven: " + df.format(dataMineResults.totalMillage);
        info += "\nTotal business miles: " + df.format(dataMineResults.businessMillage);
        info += "\nBusiness mile percentage: " + df.format(dataMineResults.businessMilePercent * 100) + "%";
        double deduction = dataMineResults.businessMillage * mileDeduction(start);
        info += "\nTotal business mile deduction: $" + df.format(deduction);
        info += "\nTotal taxable income: $" + df.format(dataMineResults.netProfit - deduction);
        statsDump.setText(info);


//        Long min = null;
//        Float max = null;
//        List<Entry> entryList = new ArrayList<>();
//        double moneyMade = 0;
//        // Now starts with min
////        Collections.reverse(dataMineResults.events);
//        for (Event event : dataMineResults.events){
//            if (min == null)
//                min = event.timeStamp;
//
//            float ftime = floatifyTime(event.timeStamp - min);
//
//            if (event.variety == Event.EventVariety.Income)
//                entryList.add(new Entry(ftime, (float) (moneyMade+=event.moneyAmount)));
////            if (event.variety == Event.EventVariety.GasEvent)
////                entryList.add(new Entry(ftime, (float) (moneyMade-=event.moneyAmount * dataMineResults.businessMilePercent)));
//            max = ftime;
//        }
//
//
//
//        chart.invalidate();

    }
    AdapterView.OnItemSelectedListener timeFrameChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            switch (position){
                case 0:
                    genRange(System.currentTimeMillis() - 1000L*60L*60L*24L*7L, System.currentTimeMillis());
                    break;
                case 1:
                    genRange(System.currentTimeMillis() - 1000L*60L*60L*24L*30L, System.currentTimeMillis());
                    break;
                case 2:
                    genRange(System.currentTimeMillis() - 1000L*60L*60L*24L*365L, System.currentTimeMillis());
                    break;
                case 3:
                    genRange(calendar.getTimeInMillis(), System.currentTimeMillis());
                    break;
                case 4:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    genRange(calendar.getTimeInMillis(), System.currentTimeMillis());
                    break;
                case 5:
                    calendar.set(Calendar.DAY_OF_MONTH, 0);
                    genRange(calendar.getTimeInMillis(), System.currentTimeMillis());
                    break;
                case 6:
                    calendar.set(Calendar.DAY_OF_YEAR, 0);
                    genRange(calendar.getTimeInMillis(), System.currentTimeMillis());
                    break;
                case 7:
                    calendar.set(Calendar.DAY_OF_MONTH, 0);
                    EventEditUtils.getDate(getContext(), calendar.getTimeInMillis(), "Select a start date", start -> {
                        EventEditUtils.getDate(getContext(), start + 1000L * 60L * 60L * 24L , "Select an end date", end -> {
                            if (end <= start){
                                parent.setSelection(0);
                                Toast.makeText(getContext(), "End must be after Start", Toast.LENGTH_LONG).show();
                                return;
                            }
                            genRange(start, end);
                        });
                    });
                    break;
                default:
                    assert false;
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private static String[] timeframeSelOpts = new String[]{
            "Last 7 days", // 0
            "Last 30 days", // 1
            "Last 365 days", // 2
            "This calender day", // 3
            "This calender week", // 4
            "This calender month", // 5
            "This calender year", // 6
            "Custom"              // 7
    };
    Spinner timeframeSelect;
    boolean isDark;
    LineChart chart;
    TextView statsDump;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        UiModeManager uiModeManager = (UiModeManager) getContext().getSystemService(Context.UI_MODE_SERVICE);
        isDark = uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats, container, false);

        chart = v.findViewById(R.id.chart);
        statsDump = v.findViewById(R.id.statsDump);

        timeframeSelect = v.findViewById(R.id.timeframeSelect);
        timeframeSelect.setOnItemSelectedListener(timeFrameChange);

        ArrayAdapter<String> timeFrameAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, timeframeSelOpts );
        timeframeSelect.setAdapter(timeFrameAdaptor);


//        genIncome(chart, isDark ? Color.WHITE : Color.BLACK);
//        chart.getDescription().setEnabled(false);
//        chart.getLegend();
//        chart.invalidate();

        return v;
    }
}