package com.github.heronerin.secureroute;

import static com.github.heronerin.secureroute.Client.Mode.LoggedOut;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Client {
    public static Client instance = null;


    public enum Mode{
        LoggedOut,
        LoggedIn,
        NeedsInit
    }
    public Mode mode;

    private boolean isServerAlive;
    public boolean isServerAlive(){return isServerAlive;};
    public final String URL;
    private String name;

    private SharedPreferences sharedPref;

    private boolean allowOnData;
    public boolean allowOnData(){return allowOnData;}

    private OkHttpClient http;
    Client(Context context){
        http = new OkHttpClient();
        URL = context.getString(R.string.serverBase);

        sharedPref = context.getSharedPreferences(
                "client", Context.MODE_PRIVATE);
        name = sharedPref.getString("name", null);
        allowOnData = sharedPref.getBoolean("allowData", true);
        if (name == null){
            mode = LoggedOut;
        }
        UserState.updateNetwork(context);
        if ((allowOnData && UserState.haveConnectedMobile) || UserState.haveConnectedWifi){
            checkServer();
        }

    }
    public Thread serverCheck = null;
    Thread checkServer(){
        if (serverCheck != null && serverCheck.isAlive()) serverCheck.interrupt();

        serverCheck =  new Thread(() -> {
            Request request = new Request.Builder()
                    .url(URL + "generate_204")
                    .build();
            try (Response response = http.newCall(request).execute()) {
                isServerAlive = response.code() == 204;
            } catch (IOException e) {
                isServerAlive = false;
            }
            
        });
        serverCheck.start();
        return serverCheck;
    }

    public static Client getOrInit(Context context){
        if (instance == null)
            instance = new Client(context);
        return instance;
    }

}
