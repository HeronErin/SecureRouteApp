package com.github.heronerin.secureroute;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CameraManager {
    public static CameraManager instance = null;
    private Runnable onExit = null;
    public AppCompatActivity activity;

    CameraManager(AppCompatActivity _mainActivity){
        activity =_mainActivity;
    }
    public String getTempPath(){ return DataBase.getOrCreate(activity).databaseUri+"/temp.json"; }
    public void openTemp(@Nullable Runnable _onExit){
        onExit=_onExit;

        activity.runOnUiThread(()->{
            FragmentContainerView frag = activity.findViewById(R.id.imgViewFragContainer);
            frag.setVisibility(View.VISIBLE);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(frag.getId(),  ImageViewerFragment.newInstance(getTempPath(), true))
                    .commit();
        });
    }
    public void handleExit(){
        activity.runOnUiThread(()->{
            FragmentContainerView frag = activity.findViewById(R.id.imgViewFragContainer);
            frag.setVisibility(View.GONE);
            if (onExit != null) onExit.run();
            onExit = null;
        });
    }
    public JSONArray getTempJsonArray(){
        StringBuilder sb = new StringBuilder();

        String path = CameraManager.instance.getTempPath();

        if (path.startsWith("file:/")) path=path.substring("file:/".length());

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(path))) {

            int b;
            while (-1 != (b = inputStream.read())) {
                sb.append((char) b);
            }
            JSONObject parcedObj = new JSONObject(sb.toString());
            return parcedObj.getJSONArray("imgs");
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return new JSONArray();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void putTempJsonArray(JSONArray jsonArray) throws IOException {
        String path = CameraManager.instance.getTempPath();

        if (path.startsWith("file:/")) path=path.substring("file:/".length());
        File file = new File(path);
        if (file.exists()) file.delete();

        if (jsonArray == null) return;

        file.createNewFile();

        try(OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            os.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
        }

    }

}
