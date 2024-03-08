package com.github.heronerin.secureroute.interactions;

import static com.github.heronerin.secureroute.interactions.Event.EventVariety.*;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class Event {
    public enum EventVariety{
        Empty,
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
    public double expenseValue;
    @Nullable public String noteData;
    @Nullable public String imageUri;
    public Event(EventVariety _variety, UUID _eventId, long _timeStamp, double _expenseValue, @Nullable String _noteData, @Nullable String _imageUri){
        this.variety = _variety;
        this.eventId = _eventId;
        this.timeStamp = _timeStamp;
        this.expenseValue = _expenseValue;
        this.noteData = _noteData;
        this.imageUri = _imageUri;
    }
    public JSONObject toJson() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("v", variety.toString());
        jsonObject.put("uuid_msig", eventId.getMostSignificantBits());
        jsonObject.put("uuid_lsig", eventId.getLeastSignificantBits());
        jsonObject.put("time", timeStamp);
        jsonObject.put("$", expenseValue);
        jsonObject.put("n", noteData);
        jsonObject.put("i", imageUri);

        return jsonObject;
    }
    public static Event fromJson(JSONObject jsonObject) throws JSONException{
        return new Event(
                EventVariety.valueOf(jsonObject.getString("v")),
                new UUID(jsonObject.getLong("uuid_msig"), jsonObject.getLong("uuid_lsig")),
                jsonObject.getLong("time"),
                jsonObject.getDouble("$"),
                jsonObject.getString("n"),
                jsonObject.getString("i")
        );
    }

}
