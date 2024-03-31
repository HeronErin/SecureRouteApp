package com.github.heronerin.secureroute;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public static String formatMillisecondsToTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "").append(", ");
        }
        if (hours > 0) {
            sb.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(", ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(", ");
        }
        sb.append(seconds).append(" second").append(seconds > 1 ? "s" : "");

        return sb.toString();
    }


    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[512*1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static void setLastUpdate(Context context){
        SharedPreferences sp = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putLong("last edited", System.currentTimeMillis());
        e.apply();
    }
    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }
}
