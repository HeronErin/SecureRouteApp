package com.github.heronerin.secureroute;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UserState {
    public static boolean haveConnectedWifi;
    public static boolean haveConnectedMobile;
    public static boolean connectionavailable;
    public static void updateNetwork(Context context) {
        haveConnectedWifi = false;
        haveConnectedMobile = false;
        connectionavailable = false;

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        NetworkInfo informationabtnet = cm.getActiveNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;

            if (informationabtnet != null && informationabtnet.isAvailable()
                        && informationabtnet.isConnected())
                    connectionavailable = true;
        }
    }
}
