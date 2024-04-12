package com.github.heronerin.secureroute.events;

import android.database.Cursor;
import android.util.Pair;

import com.github.heronerin.secureroute.DataBase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EventDataMineUtils {
    static final String TAG = "EventDataMineUtils";
    public static class DataMineResults{

        public List<Event> events;
        public double grossProfit = 0;
        public double netProfit = 0;
        public double totalExpenses = 0;
        public double moneySpentOnGas = 0;
        public int totalTrips = 0;
        public long minOdometer = Long.MAX_VALUE;
        public long maxOdometer = 0;
        public long totalMillage = 0;
        public long businessMillage = 0;
        public long workTime = 0;
        public double businessMilePercent = 0;
    }

    static public void makeRanges(List<Event> sortedEvents){
        int colorCount = 0;
        List<Pair<Integer, Event>> activeRanges = new ArrayList<>();

        for (Event event: sortedEvents){
            if (Event.isRangeStart(event.variety)){
                activeRanges.add(new Pair<>(colorCount++ % event.rangeCache.length, event));
            }
            if (Event.isRangeEnd(event.variety) && event.databaseId != -1){
                for (int i = 0; i < activeRanges.size(); i++){
                    Pair<Integer, Event> activeRange = activeRanges.get(i);
                    if (activeRange.second.associatedPair == -1) continue;
                    if (activeRange.second.associatedPair != event.databaseId) continue;

                    activeRanges.remove(i);
                    break;
                }
            }

            event.rangeCache = new boolean[]{false, false, false, false, false};
            event.cachedRanges.clear();
            for (Pair<Integer, Event> activeRange : activeRanges ){
                event.rangeCache[activeRange.first] = true;
                event.cachedRanges.add(activeRange.second);
            }
        }
    }
    public synchronized List<Event> eventsByCursor(Cursor cursor) throws JSONException{
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext())
            events.add(DataBase.eventFromCursor(cursor));
        return events;
    }
    public static double mileDeduction(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        if (c.get(Calendar.YEAR) == 2023)
            return 65.5D / 100D;
        return 67D / 100D;
    }

    static public DataMineResults dataMineFromEvents(List<Event> events) {
        List<Event> sortedEvents =  new ArrayList<>(events);
        Collections.sort(sortedEvents, (o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp));
        DataMineResults dataMineResults = new DataMineResults();
        dataMineResults.events = sortedEvents;
        for (Event event : sortedEvents){
            if (event.variety == Event.EventVariety.Income)
                dataMineResults.grossProfit += event.moneyAmount;

            if (event.variety == Event.EventVariety.Expense)
                dataMineResults.totalExpenses += event.moneyAmount;

            if (event.variety == Event.EventVariety.GasEvent)
                dataMineResults.moneySpentOnGas += event.moneyAmount;

            if (event.variety == Event.EventVariety.TripStart || event.variety == Event.EventVariety.FullTrip)
                dataMineResults.totalTrips++;
            if (event.variety == Event.EventVariety.FullTrip){
                dataMineResults.businessMillage += (long) event.sumFullTripMiles();
            }
            if (event.variety == Event.EventVariety.TripStart && event.associatedPair != -1){
                Event pair = DataBase.instance.getEventById(event.associatedPair);
                if (pair != null) {
                    dataMineResults.businessMillage += pair.odometer - event.odometer;
                    dataMineResults.workTime += pair.timeStamp - event.timeStamp;
                }
            }

            if (event.odometer != null && event.odometer > 0L){
                dataMineResults.minOdometer = Long.min(dataMineResults.minOdometer, event.odometer);
                dataMineResults.maxOdometer = Long.max(dataMineResults.maxOdometer, event.odometer);
            }
        }
        dataMineResults.totalMillage = dataMineResults.maxOdometer - dataMineResults.minOdometer;
        dataMineResults.businessMilePercent = dataMineResults.totalMillage == 0 ? 0 : Double.valueOf(dataMineResults.businessMillage) / Double.valueOf(dataMineResults.totalMillage);
        dataMineResults.totalExpenses += dataMineResults.businessMilePercent * Double.valueOf(dataMineResults.moneySpentOnGas);


        dataMineResults.netProfit = dataMineResults.grossProfit - dataMineResults.totalExpenses;


        return dataMineResults;
    }

}
