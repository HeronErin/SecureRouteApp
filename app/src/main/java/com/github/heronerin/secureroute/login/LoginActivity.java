package com.github.heronerin.secureroute.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.Client;
import com.github.heronerin.secureroute.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Client.getOrInit(this);
        findViewById(R.id.newUser).setOnClickListener((view)->{
            Intent i = new Intent(this, NewUserActivity.class);
            startActivity(i);
        });
        findViewById(R.id.ExistingUser).setOnClickListener((view)->{
            Intent i = new Intent(this, ExistingUserActivity.class);
            startActivity(i);
        });
    }
}