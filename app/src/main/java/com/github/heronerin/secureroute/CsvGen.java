package com.github.heronerin.secureroute;

import android.content.Context;
import android.util.Log;

import com.github.heronerin.secureroute.events.Event;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CsvGen {

    public static String convertUnixTimeToUTCDateTime(long unixTimestamp) {
        Date date = new Date(unixTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MMM/d/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String[] INCOME_HEADER = new String[]{"Date (HOUR:MINUTE Month/day/year in UTC)", "Amount", "Note"};


    public static File incomeStatements(Context context, long start, long end) throws IOException {
        DataBase db = DataBase.getOrCreate(context);
        File temp = File.createTempFile("incomeStatement", ".csv");
        try(FileWriter fileWriter = new FileWriter(temp)){
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(INCOME_HEADER);
            CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);

            for (Event incomeEvent : db.getInTimeFrameOfVariety(new Event.EventVariety[]{Event.EventVariety.Income}, start, end)){
                csvFilePrinter.printRecord(
                        convertUnixTimeToUTCDateTime(incomeEvent.timeStamp),
                        incomeEvent.moneyAmount,
                        incomeEvent.noteData
                );
            }


            csvFilePrinter.close(true);
        }

        return temp;
    }

    public static String[] BUSINESS_HEADER = new String[]{"Date (HOUR:MINUTE Month/day/year in UTC)", "Amount of miles", "Time taken (HOURS:MINUTES:SECONDS)", "Note"};
    public static File businessMiles(Context context, long start, long end) throws IOException {
        DataBase db = DataBase.getOrCreate(context);
        File temp = File.createTempFile("businessMiles", ".csv");
        try(FileWriter fileWriter = new FileWriter(temp)){
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(BUSINESS_HEADER);
            CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);

            for (Event tripEvent : db.getInTimeFrameOfVariety(
                    new Event.EventVariety[]{
                            Event.EventVariety.TripStart,
                            Event.EventVariety.FullTrip
                    },
                    start, end)
            ){
                String note = "";
                double miles = 0;
                double timeElapsed = 0;
                if (tripEvent.variety == Event.EventVariety.TripStart && tripEvent.associatedPair != -1){
                    Event associatedPair = db.getEventById(tripEvent.associatedPair);
                    note = tripEvent.noteData;
                    if (associatedPair != null){
                        timeElapsed = Double.valueOf(associatedPair.timeStamp - tripEvent.timeStamp);
                        miles = Double.valueOf(associatedPair.odometer) - Double.valueOf(tripEvent.odometer);
                    }
                }
                if (tripEvent.variety == Event.EventVariety.FullTrip){
                    miles = tripEvent.sumFullTripMiles();
                    note = tripEvent.getFullTripNote();
                }
                double seconds = timeElapsed / 1000;
                double minutes = seconds / 60;
                double hours = minutes / 60;

                csvFilePrinter.printRecord(
                        convertUnixTimeToUTCDateTime(tripEvent.timeStamp),
                        miles,
                        ((int) hours) + ":" + (((int)minutes) % 60)+ ":" + (((int) seconds) % 60),
                        note
                );
            }


            csvFilePrinter.close(true);
        }
        try(FileInputStream ff = new FileInputStream(temp)){
            ByteArrayOutputStream bb = new ByteArrayOutputStream();
            TripUtils.copy(ff, bb);
            Log.d("asd", new String(bb.toByteArray()));
        }

        return temp;
    }
}
