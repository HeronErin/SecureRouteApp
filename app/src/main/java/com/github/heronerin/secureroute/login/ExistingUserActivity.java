package com.github.heronerin.secureroute.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.Client;
import com.github.heronerin.secureroute.R;

public class ExistingUserActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_existing_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Client.getOrInit(this);

        findViewById(R.id.userLoginBtn).setOnClickListener((v)->{
            new Thread(()->Client.instance.handleLogin(
                    this,
                    ((EditText) findViewById(R.id.ExistingUserName)).getText().toString(),
                    ((EditText) findViewById(R.id.ExistingUserPassword)).getText().toString()
                    )).start();
            findViewById(R.id.userLoginBtn).setVisibility(View.INVISIBLE);
        });

    }
}