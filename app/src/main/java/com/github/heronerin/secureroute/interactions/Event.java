package com.github.heronerin.secureroute.interactions;

import static com.github.heronerin.secureroute.interactions.Event.EventVariety.*;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class Event {
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

}
