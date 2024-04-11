package com.github.heronerin.secureroute.events;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.TripUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventEditUtils {


    public static MenuItem.OnMenuItemClickListener editNote(Context context, Event event){

        return item -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Edit your note");
            EditText editText = new EditText(context);
            editText.setSingleLine(false);
            editText.setText(event.noteData);
            editText.setHint("Enter the note data");
            alert.setView(editText);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                event.noteData = editText.getText().toString();
                DataBase.getOrCreate(context).updateEvent(event);

                TripUtils.setLastUpdate(context);
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();

            return true;
        };
    }
    public static MenuItem.OnMenuItemClickListener editMoney(Context context, Event event){

        return item -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Edit money amount");
            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            editText.setText(String.valueOf(event.moneyAmount));
            alert.setView(editText);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                event.moneyAmount = Double.valueOf(editText.getText().toString());
                DataBase.getOrCreate(context).updateEvent(event);

                TripUtils.setLastUpdate(context);
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();
            return true;
        };
    }
    public static MenuItem.OnMenuItemClickListener editOdometer(Context context, Event event){

        return item -> {
            if (event.odometer == null) return true;

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Edit odometer value");
            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setText(String.valueOf(event.odometer));
            alert.setView(editText);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                event.odometer = Long.valueOf(editText.getText().toString());
                DataBase.getOrCreate(context).updateEvent(event);
                TripUtils.setLastUpdate(context);
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();
            return true;
        };
    }
    public interface DateCallback{
        void run(Long date);
    }
    public static void getDate(Context context, long startingPoint, String title, DateCallback dateCallback){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startingPoint);

        DatePicker datePicker = new DatePicker(context);
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        alert.setView(datePicker);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Calendar.YEAR, datePicker.getYear());
            calendar1.set(Calendar.MONTH, datePicker.getMonth());
            calendar1.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

            dateCallback.run(calendar1.getTimeInMillis());

        });

        alert.show();
    }
    public static MenuItem.OnMenuItemClickListener editTime(Context context, Event event){

        return item -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Edit your date");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.timeStamp);

            DatePicker datePicker = new DatePicker(context);
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            alert.setView(datePicker);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                AlertDialog.Builder alert2 = new AlertDialog.Builder(context);
                alert2.setTitle("Edit your time");

                TimePicker timePicker = new TimePicker(context);
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                alert2.setView(timePicker);

                alert2.setNegativeButton("Cancel", (dialog_, whichButton_) -> {});
                alert2.setPositiveButton("Ok", (dialog_, whichButton_) -> {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.YEAR, datePicker.getYear());
                    calendar1.set(Calendar.MONTH, datePicker.getMonth());
                    calendar1.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

                    calendar1.set(Calendar.HOUR, timePicker.getCurrentHour());
                    calendar1.set(Calendar.MINUTE, timePicker.getCurrentMinute());

                    event.timeStamp = calendar1.getTimeInMillis();
                    DataBase.getOrCreate(context).updateEvent(event);

                    TripUtils.setLastUpdate(context);
                });


                alert2.show();

            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();

            return true;
        };
    }
    public static void confirm(Runnable onConfirm, Runnable onCancel, String title, String body, Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        TextView tv = new TextView(context);
        tv.setText(body);
        tv.setPadding(20, 20, 20, 20);
        alert.setView(tv);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> onConfirm.run());
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> onCancel.run());

        alert.show();
    }
    public static void confirm(Runnable onConfirm, String title, String body, Context context){ confirm(onConfirm, ()->{}, title, body, context); }

    public static void thisOrThat(String title, String body, Context context, String thisStr, Runnable thisCallback, String thatStr, Runnable thatCallback){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        TextView tv = new TextView(context);
        tv.setText(body);
        tv.setPadding(20, 20, 20, 20);
        alert.setView(tv);

        alert.setPositiveButton(thisStr, (dialog, whichButton) -> thisCallback.run());
        alert.setNegativeButton(thatStr, (dialog, whichButton) -> thatCallback.run());

        alert.show();
    }

    public static MenuItem.OnMenuItemClickListener deleteEvent(Context context, Event event){
        return item -> {
            confirm(()-> confirm(()->{
                DataBase db = DataBase.getOrCreate(context);
                db.deleteEvent(event);
                TripUtils.setLastUpdate(context);
            }, "Are truly you sure?", "This actually can't be undone!", context),
                    "Are you sure?", "Deleting an event is something that can't be undone!", context);
            return true;
        };
    }
    public static MenuItem.OnMenuItemClickListener transmuteVariety(Context context, Event event, Event.EventVariety[] varietyList){

        return item -> {
            if (event.odometer == null) return true;

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Edit type");
            Spinner spinner = new Spinner(context);
            List<String> strings = new ArrayList<>();
            strings.add(event.variety.toString());

            for (Event.EventVariety variety : varietyList)
                if (!variety.toString().equals(event.variety.toString()))
                    strings.add(variety.toString());

            ArrayAdapter<String> varietyAdaptor = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strings);
            spinner.setAdapter(varietyAdaptor);
            alert.setView(spinner);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                String selected = strings.get(spinner.getSelectedItemPosition());
                event.variety = Event.EventVariety.valueOf(selected);

                DataBase.getOrCreate(context).updateEvent(event);
                TripUtils.setLastUpdate(context);
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();
            return true;
        };
    }
    public static MenuItem.OnMenuItemClickListener transmuteMonetaryAmount(Context context, Event event){
        return transmuteVariety(
                context,
                event,
                new Event.EventVariety[]{
                        Event.EventVariety.Income,
                        Event.EventVariety.Expense,
                        Event.EventVariety.JobExpense
                }
        );
    }
    public static MenuItem.OnMenuItemClickListener transmute(Context context, Event event){
        return transmuteVariety(
                context,
                event,
                Event.EventVariety.values()
        );
    }
}
