package com.github.heronerin.secureroute;

import static com.github.heronerin.secureroute.Client.Mode.LoggedIn;
import static com.github.heronerin.secureroute.Client.Mode.LoggedOut;
import static com.github.heronerin.secureroute.Client.Mode.NeedsInit;

import static java.net.CookiePolicy.ACCEPT_ALL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

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
    public String name;

//    private synchronized CookieManager cookieManager = new CookieManager();

    private SharedPreferences sharedPref;

    private boolean allowOnData;
    public boolean allowOnData(){return allowOnData;}

    public OkHttpClient http;

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
        mode = Mode.valueOf(sharedPref.getString("mode", LoggedOut.toString()));

        http = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();


        UserState.updateNetwork(context);
        if ((allowOnData && UserState.haveConnectedMobile) || UserState.haveConnectedWifi){
            checkServer();
        }
    }
    public Thread serverCheck = null;
    public void flushCookies(){
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
        e.putString("mode", mode.toString());
        e.apply();
    }
    Thread checkServer(){
        if (serverCheck != null && serverCheck.isAlive()) serverCheck.interrupt();

        serverCheck =  new Thread(() -> {
            Request request = new Request.Builder()
                    .url(URL + "connection_test")
                    .build();
            try (Response response = http.newCall(request).execute()) {
                isServerAlive = response.code() == 204 || response.code() == 203;
                if (response.code() == 203 && mode == LoggedIn){
                    mode = LoggedOut;
                    flushCookies();
                }
            } catch (IOException e) {
                isServerAlive = false;
            }
//            flushCookie();

            
        });
        serverCheck.start();
        return serverCheck;
    }

    public static Client getOrInit(Context context){
        if (instance == null)
            instance = new Client(context);
        return instance;
    }
    public void handleTpinLogin(AppCompatActivity context, String tpin, String name){
        try {
            JSONObject input = new JSONObject();
            input.put("mode", "tpin");
            input.put("pin", tpin);
            input.put("name", name);
            Request request = new Request.Builder()
                    .url(Client.instance.URL + "auth")
                    .post(new RequestBody() {
                        @Nullable
                        @Override
                        public MediaType contentType() {
                            return MediaType.get("application/json");
                        }
                        @Override
                        public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {
                            bufferedSink.write(input.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    })
                    .build();
            try (Response response = Client.instance.http.newCall(request).execute()) {
                Client.instance.flushCookies();

                assert response.body() != null;
                JSONObject jsonObject = new JSONObject(response.body().string());

                if (jsonObject.getString("result").equals("failure")){
                    context.runOnUiThread(()-> {
                        Toast.makeText(context, "Login not recognized", Toast.LENGTH_LONG).show();
                        context.findViewById(R.id.pinEnterBtn).setVisibility(View.VISIBLE);
                    });
                    return;
                }
                if (jsonObject.getString("mode").equals("creation")){
                    SharedPreferences.Editor e = sharedPref.edit();
                    name = jsonObject.getString("name");
                    e.putString("name", name);
                    mode = NeedsInit;
                    e.putString("mode", mode.toString());
                    e.apply();
                    Intent i = new Intent(context, LoginLauncher.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(i);
                    context.finish();
                }

            } catch (AssertionError | JSONException | IOException e) {
                e.printStackTrace();
                context.runOnUiThread(()->{
                    context.findViewById(R.id.pinEnterBtn).setVisibility(View.VISIBLE);

                    Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                });
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleLogin(AppCompatActivity context, String username, String password){
        try {
            JSONObject input = new JSONObject();
            input.put("mode", "uauth");
            input.put("password", hashPass(password));
            input.put("name", username);
            Request request = new Request.Builder()
                    .url(Client.instance.URL + "auth")
                    .post(new RequestBody() {
                        @Nullable
                        @Override
                        public MediaType contentType() {
                            return MediaType.get("application/json");
                        }
                        @Override
                        public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {
                            bufferedSink.write(input.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    })
                    .build();
            try (Response response = Client.instance.http.newCall(request).execute()) {
                Client.instance.flushCookies();

                assert response.body() != null;
                JSONObject jsonObject = new JSONObject(response.body().string());

                if (jsonObject.getString("result").equals("failure")){
                    context.runOnUiThread(()-> {
                        Toast.makeText(context, "Login not recognized", Toast.LENGTH_LONG).show();
                        context.findViewById(R.id.userLoginBtn).setVisibility(View.VISIBLE);
                    });
                    return;
                }
                if (jsonObject.getString("mode").equals("loggedin")){
                    SharedPreferences.Editor e = sharedPref.edit();
                    name = jsonObject.getString("name");
                    e.putString("name", name);
                    mode = LoggedIn;
                    e.putString("mode", mode.toString());
                    e.apply();
                    Intent i = new Intent(context, LoginLauncher.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(i);
                    context.finish();
                }

            } catch (AssertionError | JSONException | IOException e) {
                e.printStackTrace();
                context.runOnUiThread(()->{
                    context.findViewById(R.id.userLoginBtn).setVisibility(View.VISIBLE);

                    Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                });
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleNewPass(AppCompatActivity context, String hash) {
        try {
            JSONObject input = new JSONObject();
            input.put("password", hash);
            Request request = new Request.Builder()
                    .url(Client.instance.URL + "init")
                    .post(new RequestBody() {
                        @Nullable
                        @Override
                        public MediaType contentType() {
                            return MediaType.get("application/json");
                        }
                        @Override
                        public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {
                            bufferedSink.write(input.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    })
                    .build();
            Response response = Client.instance.http.newCall(request).execute();
            JSONObject output = new JSONObject(response.body().string());
            if (output.getString("result").equals("failure")){
                context.runOnUiThread(()->Toast.makeText(context, "Issue logging in, your account may have already been created", Toast.LENGTH_LONG).show());
                response.close();
                return;
            }
            mode = LoggedIn;
            flushCookies();

            Intent i = new Intent(context, LoginLauncher.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
            context.finish();
            response.close();

        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            context.runOnUiThread(()->Toast.makeText(context, "Network issue", Toast.LENGTH_LONG).show());
        }

    }
    public static String hashPass(String s){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sha = digest.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : sha) {
                sb.append(String.format("%02X ", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}

