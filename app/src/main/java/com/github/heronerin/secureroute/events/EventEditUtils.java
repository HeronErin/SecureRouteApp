package com.github.heronerin.secureroute.events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.github.heronerin.secureroute.DataBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();
            return true;
        };
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
                });


                alert2.show();

            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();

            return true;
        };
    }
    public static void confirm(Runnable onConfirm, String title, String body, Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        TextView tv = new TextView(context);
        tv.setText(body);
        tv.setPadding(20, 20, 20, 20);
        alert.setView(tv);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> onConfirm.run());
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

        alert.show();
    }
    public static MenuItem.OnMenuItemClickListener deleteEvent(Context context, Event event){
        return item -> {
            confirm(()-> confirm(()->{
                DataBase db = DataBase.getOrCreate(context);
                db.deleteEvent(event);
            }, "Are truly you sure?", "This actually can't be undone!", context),
                    "Are you sure?", "Deleting an event is something that can't be undone!", context);
            return true;
        };
    }
    public static MenuItem.OnMenuItemClickListener transmuteVariety(Context context, Event event, List<Event.EventVariety> varietyList){

        return item -> {
            if (event.odometer == null) return true;

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Edit type");
            Spinner spinner = new Spinner(context);
            List<String> strings = new ArrayList<>();
            
            ArrayAdapter<String> varietyAdaptor = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strings);
            spinner.setAdapter(varietyAdaptor);


            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
//                event.odometer = Long.valueOf(editText.getText().toString());
//                DataBase.getOrCreate(context).updateEvent(event);
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

            alert.show();
            return true;
        };
    }
}
