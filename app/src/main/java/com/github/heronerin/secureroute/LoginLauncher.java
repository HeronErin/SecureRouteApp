package com.github.heronerin.secureroute;

import static com.github.heronerin.secureroute.Client.Mode.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.heronerin.secureroute.login.EnterPwd;
import com.github.heronerin.secureroute.login.LoginActivity;

public class LoginLauncher extends AppCompatActivity {


    void handleRefresh(){
        TextView textView = findViewById(R.id.mainMenuText);
        textView.setText("Connecting...");

        new Thread(()->{
            try {
                if (Client.instance.serverCheck == null || !Client.instance.serverCheck.isAlive()) Client.instance.checkServer();

                Client.instance.serverCheck.join();
            } catch (InterruptedException e) {throw new RuntimeException(e);}
            runOnUiThread(()->{
                ((SwipeRefreshLayout)findViewById(R.id.swiperefresh)).setRefreshing(false);
                TextView tv = findViewById(R.id.mainMenuText);

                if ((Client.instance.mode == NeedsInit || Client.instance.mode == LoggedOut) && !(UserState.haveConnectedWifi || (
                        UserState.haveConnectedMobile && Client.instance.allowOnData()
                ))){
                    tv.setText("Error: Internet not available, and required for logging in.");
                    return;
                }
                if ((Client.instance.mode == NeedsInit || Client.instance.mode == LoggedOut) && !Client.instance.isServerAlive()){
                    tv.setText("Error: User needs logged in, but the server can't be contacted.");
                    return;
                }
                switch (Client.instance.mode){
                    case LoggedOut:
                        Intent i = new Intent(this, LoginActivity.class);
                        startActivity(i);
                        break;
                    case LoggedIn:
                        tv.setText("SHIT IT WORKED");
                        break;
                    case NeedsInit:
                        Intent ii = new Intent(this, EnterPwd.class);
                        startActivity(ii);
                }

            });
        }).start();
    }

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

        Client.getOrInit(this);
        UserState.updateNetwork(this);
        handleRefresh();
        ((SwipeRefreshLayout)findViewById(R.id.swiperefresh)).setOnRefreshListener(this::handleRefresh);

    }
}