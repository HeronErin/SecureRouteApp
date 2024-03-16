package com.github.heronerin.secureroute;

import static androidx.fragment.app.FragmentManagerKt.commit;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentContainerView;

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

}
