package com.github.heronerin.secureroute.events;

import android.database.Cursor;
import android.util.Log;
import android.util.Pair;

import com.github.heronerin.secureroute.DataBase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EventDataMineUtils {
    static final String TAG = "EventDataMineUtils";
    public static class DataMineResults{

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

    static synchronized public DataMineResults dataMineFromEvents(List<Event> events) {

        DataMineResults dataMineResults = new DataMineResults();



        List<Event> sortedEvents =  new ArrayList<>(events);
        Collections.sort(sortedEvents, Comparator.comparingLong(o -> o.timeStamp));

        makeRanges(sortedEvents);

        return dataMineResults;
    }

}
