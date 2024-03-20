package com.github.heronerin.secureroute;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TripUtils {
    public static final String ONGOING_TRIP_CHANNEL = "Ongoing Trip";
    public static final int TRIP_NOTIFICATION_ID = 6969;

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ONGOING_TRIP_CHANNEL, "Ongoing trip reminder", importance);
            channel.setDescription("An ongoing notification to remind you to end a trip");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void startTrip(Context context) {
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ONGOING_TRIP_CHANNEL)
                .setSmallIcon(R.drawable.car_icon)
                .setContentTitle("Ongoing trip")
                .setContentText("You have a trip ongoing, remember to end it later")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You have a trip ongoing, remember to end it later"))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(context).notify(TRIP_NOTIFICATION_ID, builder.build());
    }
    public static void endTrip(Context context){
        NotificationManagerCompat.from(context).cancel(TRIP_NOTIFICATION_ID);
    }
}
