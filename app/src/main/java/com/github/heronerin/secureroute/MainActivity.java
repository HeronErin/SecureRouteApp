package com.github.heronerin.secureroute;

import static com.github.heronerin.secureroute.TripUtils.startTrip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.tabs.AddFragment;
import com.github.heronerin.secureroute.tabs.EventList;
import com.github.heronerin.secureroute.tabs.SettingsFragment;
import com.github.heronerin.secureroute.tabs.StatsFragment;
import com.github.heronerin.secureroute.tabs.UploadFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        if (v.getId() == R.id.addBtn && R.id.addBtn == currentTab){
            boolean doReset = ((AddFragment)fragments[2]).onAddClick();
            if (!doReset)
                return;
            v = findViewById(R.id.lineItemBtn);

            // Or else AddFragment get corrupted... Likely due to the fragment on AddFragment gets removed
            fragments[2] = AddFragment.newInstance();

        }
//        if (v.getId() == currentTab)
//            return;
        if (currentTab != 0)
            ((ImageButton) findViewById(currentTab)).clearColorFilter();
        if (v.getId() != R.id.addBtn)
            ((ImageButton) v).setColorFilter(Color.CYAN);
        else
            ((ImageButton) v).setColorFilter(Color.rgb(0xFF, 0xA5, 0));
        currentTab = v.getId();


        for(int i = 0; i < btns.length; i++){
            if (btns[i] == v.getId()){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.tabFragmentContainer, fragments[i])
                        .commit();
                break;
            }
        }
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
        String[] perms  = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            perms = Arrays.copyOf(perms, perms.length + 1);
            perms[perms.length - 1] = Manifest.permission.POST_NOTIFICATIONS;
        }
        List<String> needs = new ArrayList<>();
        boolean needsPerm = false;
        for (String perm : perms){
            boolean denied = PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, perm);
            if (denied) needs.add(perm);
            needsPerm |= denied;
        }
        if (needsPerm){
            ActivityCompat.requestPermissions((Activity) this, needs.toArray(new String[0]), 69);
        }

        for (int btn : btns)
            findViewById(btn).setOnClickListener(this::onTabClick);
        findViewById(btns[0]).callOnClick();

        CameraManager.instance = new CameraManager(this);

        updateNotification(this);
    }


    public static void updateNotification(Context context){
        Event e = DataBase.getOrCreate(context).getLastTrip();
        if (e == null)
            return;
        if (e.variety == Event.EventVariety.TripEnd){
            TripUtils.endTrip(context);
            return;
        }

        TripUtils.startTrip(context);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 69) return;
        Log.d("RES", Arrays.toString(grantResults));
        Log.d("RES", Arrays.toString(permissions));
        for (int perResult : grantResults) {
            if (perResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "These permissions are not optional!", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }

    }

}