package com.github.heronerin.secureroute;

import static com.github.heronerin.secureroute.Client.Mode.LoggedOut;

import static java.net.CookiePolicy.ACCEPT_ALL;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
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

//    private synchronized CookieManager cookieManager = new CookieManager();

    private SharedPreferences sharedPref;

    private boolean allowOnData;
    public boolean allowOnData(){return allowOnData;}

    private OkHttpClient http;

    CookieManager cookieManager;
    JavaNetCookieJar cookieJar;
    Client(Context context){
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(ACCEPT_ALL);

        cookieJar = new JavaNetCookieJar(cookieManager);

        URL = context.getString(R.string.serverBase);

        sharedPref = context.getSharedPreferences(
                "client", Context.MODE_PRIVATE);
        name = sharedPref.getString("name", null);

        try {
            JSONArray sites = new JSONArray(sharedPref.getString("client cookies", "[]"));
            for (int i = 0; i < sites.length(); i++){
                JSONArray site = sites.getJSONArray(i);
                URI uri = new URI(site.getString(0));
                for (int ii = 1; ii < site.length(); ii++){
                    JSONArray name_value = site.getJSONArray(ii);
                    cookieManager.getCookieStore().add(uri, new HttpCookie(name_value.getString(0), name_value.getString(1)));
                }
            }

        } catch (JSONException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        allowOnData = sharedPref.getBoolean("allowData", true);
        if (name == null){
            mode = LoggedOut;
        }
        http = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();


        UserState.updateNetwork(context);
        if ((allowOnData && UserState.haveConnectedMobile) || UserState.haveConnectedWifi){
            checkServer();
        }
//        Runtime.getRuntime().addShutdownHook(new Thread(this::flushCookies));

    }
    public Thread serverCheck = null;
    void flushCookies(){
        List<URI> uris = cookieManager.getCookieStore().getURIs();
        JSONArray sites = new JSONArray();
        for (int i = 0; i < uris.size(); i++){
            JSONArray site_cookies = new JSONArray();
            site_cookies.put(uris.get(i));
            List<HttpCookie> cookies = cookieManager.getCookieStore().get(uris.get(i));
            for(int ii = 0; ii < cookies.size(); ii++){
                HttpCookie c = cookies.get(0);
                JSONArray cookie = new JSONArray();
                cookie.put(c.getName());
                cookie.put(c.getValue());
                site_cookies.put(cookie);
            }
            sites.put(site_cookies);
        }
        SharedPreferences.Editor e = sharedPref.edit();
        e.putString("client cookies", sites.toString());
        e.apply();
    }
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
//            flushCookies();

            
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
