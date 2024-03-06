package com.github.heronerin.secureroute.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.heronerin.secureroute.Client;
import com.github.heronerin.secureroute.R;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EnterPwd extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_pwd);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ((TextView) findViewById(R.id.userName)).setText(Client.getOrInit(this).name);


        findViewById(R.id.newUserInfoBtn).setOnClickListener((v)->{
            TextView textView = findViewById(R.id.newUserPassword);
            if (textView.getText().length() < 5){
                Toast.makeText(this, "Password needs to be at least 5 long", Toast.LENGTH_LONG).show();
                return;
            }
            String hash = Client.hashPass(textView.getText().toString());
            new Thread(()->Client.instance.handleNewPass(this, hash)).start();

        });
    }
}