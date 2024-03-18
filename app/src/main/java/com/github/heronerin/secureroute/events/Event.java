package com.github.heronerin.secureroute.interactions;

import static com.github.heronerin.secureroute.interactions.Event.EventVariety.*;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Event {
    public static boolean isRangeStart(EventVariety v){
        return  v == ArbitraryRangeStart || v == MillageStartJob || v == MillageStartNonJob;
    }
    public static boolean isRangeEnd(EventVariety v){
        return  v == ArbitraryRangeEnd || v == MillageEndJob || v == MillageEndNonJob;
    }
    public static EventVariety getAsRangeStart(EventVariety v){
        if (v == ArbitraryRangeEnd)
            return ArbitraryRangeStart;
        if (v == MillageEndJob)
            return MillageStartJob;
        if (v == MillageEndNonJob)
            return MillageStartNonJob;
        return null;
    }
    public static String[] possibleRangeStrings = new String[]{
            ArbitraryRangeStart.toString(),
            ArbitraryRangeEnd.toString(),

            MillageStartJob.toString(),
            MillageEndJob.toString(),

            MillageStartNonJob.toString(),
            MillageEndNonJob.toString(),

    };
    public boolean[] rangeCache = new boolean[]{false, false, false, false, false};
    public List<Event> cachedRanges = new ArrayList<>();
    public enum EventVariety{
        Empty,
        ArbitraryNote,
        ArbitraryRangeStart,
        ArbitraryRangeEnd,
        MillageStartJob,
        MillageEndJob,
        MillageStartNonJob,
        MillageEndNonJob,
        ArbitraryJobExpense,
        ArbitraryExpense
    }
    public EventVariety variety = Empty;
    public int databaseId = -1;
    public UUID eventId;
    public long timeStamp;
    @Nullable public int associatedPair = -1;
    public double expenseValue;
    @Nullable public String noteData;
    @Nullable public JSONArray imageUri;
    public Event(EventVariety _variety, UUID _eventId, long _timeStamp, double _expenseValue, int _associatedPair, @Nullable String _noteData, @Nullable JSONArray _imageUri){
        this.variety = _variety;
        this.eventId = _eventId;
        this.timeStamp = _timeStamp;
        this.expenseValue = _expenseValue;
        this.associatedPair=_associatedPair;
        this.noteData = _noteData;
        this.imageUri = _imageUri;
    }


    public static void applyRanges(List<Event> events, List<Event> ranges){
        Collections.sort(events, (o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp));
        Collections.sort(ranges, (o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp));
        int pastColorStart = 0;
        int eventIndex = 0;
        int rangeIndex = 0;

        List<Pair<Integer, Integer>> rangeOpensAndColors = new ArrayList<>();
        List<Pair<Integer, Event>> colorsInUse = new ArrayList<>();

        while (eventIndex < events.size()){
            Log.w("EventSort", String.valueOf(events.get(0).timeStamp));
            Event e = events.get(eventIndex);
            e.cachedRanges.clear();

            while (rangeIndex < ranges.size() && ranges.get(rangeIndex).timeStamp <= events.get(eventIndex).timeStamp){
                Event range = ranges.get(rangeIndex);
                if (Event.isRangeStart(range.variety)){
                    Integer foundColor = null;

                    for (int c = pastColorStart; c < 5+pastColorStart; c++){
                        if (!colorsInUse.contains(c % 5)){
                            foundColor = c % 5;
                            colorsInUse.add(new Pair<>(c % 5, range));
                            break;
                        }
                    }
                    pastColorStart++;

                    rangeOpensAndColors.add(new Pair<>(range.associatedPair, foundColor));
                }else if(Event.isRangeEnd(range.variety)){
                    for (int i = 0; i < rangeOpensAndColors.size(); i++){
                        if (range.databaseId != -1 && rangeOpensAndColors.get(i).first == range.databaseId){
                            colorsInUse.remove(rangeOpensAndColors.get(i).second);
                            rangeOpensAndColors.remove(i);
                            break;
                        }
                    }
                }
                rangeIndex++;
            }
            Log.e("Color", colorsInUse.toString());
            for (Pair<Integer, Event> i : colorsInUse){
                e.rangeCache[i.first] = true;
                e.cachedRanges.add(i.second);
            }
            eventIndex++;


        }
        Collections.reverse(events);
    }
}
