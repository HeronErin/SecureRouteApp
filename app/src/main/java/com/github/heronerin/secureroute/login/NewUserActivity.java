package com.github.heronerin.secureroute.login;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.Client;
import com.github.heronerin.secureroute.R;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Client.getOrInit(this);

        findViewById(R.id.pinEnterBtn).setOnClickListener((view)->{
            findViewById(R.id.pinEnterBtn).setVisibility(View.INVISIBLE);
            new Thread(()->Client.instance.handleTpinLogin(
                    this,
                    ((EditText)findViewById(R.id.userName)).getText().toString(),
                    ((EditText)findViewById(R.id.usernameEnter)).getText().toString()
            )).start();

        });
    }
}