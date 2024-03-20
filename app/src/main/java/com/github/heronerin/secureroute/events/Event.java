package com.github.heronerin.secureroute.events;

import static com.github.heronerin.secureroute.events.Event.EventVariety.*;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.eventViewer.GasRangeViewer;
import com.github.heronerin.secureroute.eventViewer.IncomeViewer;
import com.github.heronerin.secureroute.eventViewer.NoteViewer;
import com.github.heronerin.secureroute.eventViewer.TripViewer;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;




public class Event implements Serializable {
    private static class JsonArrayHolder implements Serializable {
        private transient JSONArray jsonArray;

        public JsonArrayHolder(JSONArray _jsonArray) {
            jsonArray = _jsonArray;
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.defaultWriteObject();
            oos.writeObject(jsonArray.toString());
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();

            String jsonArrayString = (String) ois.readObject();
            try {
                jsonArray = new JSONArray(jsonArrayString);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isRangeStart(EventVariety v){
        return  v == ArbitraryRangeStart || v == MillageStartJob || v == MillageStartNonJob || v == GasEvent;
    }
    public static boolean isRangeEnd(EventVariety v){
        return  v == ArbitraryRangeEnd || v == MillageEndJob || v == MillageEndNonJob || v == GasEventEnd;
    }
    public static EventVariety getAsRangeStart(EventVariety v){
        if (v == ArbitraryRangeEnd)
            return ArbitraryRangeStart;
        if (v == MillageEndJob)
            return MillageStartJob;
        if (v == MillageEndNonJob)
            return MillageStartNonJob;
        if (v == GasEventEnd)
            return GasEvent;
        return null;
    }
    public static String[] possibleRangeStrings = new String[]{
            ArbitraryRangeStart.toString(),
            ArbitraryRangeEnd.toString(),

            MillageStartJob.toString(),
            MillageEndJob.toString(),

            MillageStartNonJob.toString(),
            MillageEndNonJob.toString(),

            GasEvent.toString(),
            GasEventEnd.toString()

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
        TripStart,
        TripEnd,
        GasEvent,
        GasEventEnd,
        MillageEndNonJob,
        JobExpense,
        Expense,

        Income




    }
    public EventVariety variety = Empty;
    public int databaseId = -1;
    public UUID eventId;
    public long timeStamp;
    @Nullable public int associatedPair = -1;
    public double moneyAmount;
    @Nullable public String noteData;
    @Nullable private JsonArrayHolder imageData;

    @Nullable public Long odometer = null;

    @Nullable
    public JSONArray getImageData() {
        if (imageData == null) return null;
        return imageData.jsonArray;
    }

    public Event(EventVariety _variety, UUID _eventId, long _timeStamp, double _moneyAmount, int _associatedPair, @Nullable String _noteData, @Nullable JSONArray _imagedata, @Nullable Long odometer){
        this.variety = _variety;
        this.eventId = _eventId;
        this.timeStamp = _timeStamp;
        this.moneyAmount = _moneyAmount;
        this.associatedPair=_associatedPair;
        this.noteData = _noteData;
        this.imageData = _imagedata == null ? null :new JsonArrayHolder(_imagedata);
        this.odometer = odometer;
//        this.imageUri = _imageUri;
    }

    public void setImageData(@Nullable JSONArray imageData) {
        this.imageData = imageData == null ? null :new JsonArrayHolder(imageData);
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
                            for (int ii = 0; ii < colorsInUse.size(); ii++){
                                if (colorsInUse.get(ii).second.associatedPair != range.databaseId) continue;
                                colorsInUse.remove(ii);
                                break;
                            }
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

    public static final int MAX_PREVIEW_SIZE = 128;
    private static String noteDataHandle(@Nullable String input){
        if (input == null) input = "";
        input=input.trim();

        String data = "Empty note";
        if (!input.isEmpty()){
            data = input;
            int nextLine = data.indexOf("\n");
            if (nextLine != -1 || data.length() > MAX_PREVIEW_SIZE){
                if (nextLine != -1) data = data.substring(0, nextLine) + "...";
                if (data.length() > MAX_PREVIEW_SIZE) data = data.substring(0, MAX_PREVIEW_SIZE) + "...";
            }
        }
        return data;
    }
    public String eventPreview(){
        if (variety == Empty)
            return "EMPTY EVENT";
        if (variety == ArbitraryNote || variety == ArbitraryRangeStart || variety == TripStart || variety == TripEnd)
            return noteDataHandle(noteData);
        if (isRangeEnd(variety))
            return "End of " + getAsRangeStart(variety).toString();
        if (variety == Income)
            return "+$"+String.valueOf(moneyAmount)+"\n"+noteDataHandle(noteData);
        if (variety == JobExpense || variety == Expense)
            return "-$"+String.valueOf(moneyAmount)+"\n"+noteDataHandle(noteData);


        return "TODO: Handle this event (" + variety.toString() + ")";
    }
    public int getIcon(){
        if (variety == ArbitraryNote){
            return R.drawable.note_icon;
        }
        if (variety == ArbitraryRangeStart)
            return R.drawable.calender_icon;
        if (variety == MillageStartJob || variety == MillageEndJob)
            return R.drawable.pump_icon;
        if (variety == Income)
            return R.drawable.green_dollar_icon;
        if (variety == JobExpense)
            return R.drawable.red_dollar_icon;
        if (variety == Expense)
            return R.drawable.yellow_dollar_icon;
        if (variety == TripStart)
            return R.drawable.green_car_icon;
        if (variety == TripEnd)
            return R.drawable.red_car_icon;
        if (variety == GasEvent)
            return R.drawable.pump_icon;
        if (variety == GasEventEnd)
            return R.drawable.graph_up;

        return R.drawable.ic_launcher_foreground;
    }

    public String encodeAsString(){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            return Base64.encodeBase64String(baos.toByteArray());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public static Event decodeFromString(String base64String){
        try {
            byte[] bytes = Base64.decodeBase64(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);

            Event instance = (Event) ois.readObject();

            ois.close();
            return instance;
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public  Class<?> getViewerClass(){
        if (variety == Expense || variety == Income || variety == JobExpense)
            return IncomeViewer.class;
        if (variety == TripStart || variety == TripEnd)
            return TripViewer.class;
        if (variety == GasEvent)
            return GasRangeViewer.class;
        return NoteViewer.class;
    }


}
