package com.github.heronerin.secureroute;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.Image;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.github.heronerin.secureroute.tabs.AddFragment;
import com.github.heronerin.secureroute.tabs.EventList;
import com.github.heronerin.secureroute.tabs.SettingsFragment;
import com.github.heronerin.secureroute.tabs.StatsFragment;
import com.github.heronerin.secureroute.tabs.UploadFragment;

public class MainActivity extends AppCompatActivity {
    private final int[] btns = new int[]{R.id.lineItemBtn, R.id.statsBtn, R.id.addBtn, R.id.settingBtn, R.id.uploadBtn};
    private final androidx.fragment.app.Fragment[] fragments = new androidx.fragment.app.Fragment[]{
            EventList.newInstance(),
            StatsFragment.newInstance(),
            AddFragment.newInstance(),
            SettingsFragment.newInstance(),
            UploadFragment.newInstance(),

    };
    int currentTab = 0;

    void onTabClick(View  v){
        if (v.getId() == currentTab)
            return;
        if (currentTab != 0)
            ((ImageButton) findViewById(currentTab)).clearColorFilter();
        ((ImageButton) v).setColorFilter(Color.CYAN);
        currentTab = v.getId();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        for(int i = 0; i < btns.length; i++){
            if (btns[i] == v.getId()){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.tabFragmentContainer, fragments[i])
                        .commit();
            }
        }
        fragmentTransaction.commit();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        for (int btn : btns)
            findViewById(btn).setOnClickListener(this::onTabClick);

        findViewById(R.id.lineItemBtn).callOnClick();


//        ((FragmentContainerView) findViewById(R.id.tabFragmentContainer)).getFra
    }

}