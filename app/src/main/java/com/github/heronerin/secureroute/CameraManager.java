package com.github.heronerin.secureroute;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentContainerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CameraManager {
    public static CameraManager instance = null;
    private Runnable onExit = null;
    private MainActivity mainActivity;

    CameraManager(MainActivity _mainActivity){
        mainActivity=_mainActivity;
    }
    public String getTempPath(){ return DataBase.getOrCreate(mainActivity).databaseUri+"/temp.json"; }
    public void openTemp(@Nullable Runnable _onExit){
        onExit=_onExit;
        mainActivity.runOnUiThread(()->{
            FragmentContainerView frag = mainActivity.findViewById(R.id.imgViewFragContainer);
            frag.setVisibility(View.VISIBLE);
            mainActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(frag.getId(),  ImageViewerFragment.newInstance(getTempPath(), true))
                    .commit();
        });
    }
    public void handleExit(){
        mainActivity.runOnUiThread(()->{
            FragmentContainerView frag = mainActivity.findViewById(R.id.imgViewFragContainer);
            frag.setVisibility(View.GONE);
            if (onExit != null) onExit.run();
            onExit = null;
        });
    }
    public JSONArray getTempJsonArray(){
        StringBuilder sb = new StringBuilder();

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(CameraManager.instance.getTempPath()))) {

            int b;
            while (-1 != (b = inputStream.read())) {
                sb.append((char) b);
            }

            return (new JSONObject(sb.toString())).getJSONArray("imgs");
        }catch (FileNotFoundException ignored){
            return new JSONArray();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

}
