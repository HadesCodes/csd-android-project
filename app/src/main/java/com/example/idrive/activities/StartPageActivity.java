package com.example.idrive.activities;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.idrive.R;

public class StartPageActivity extends AppCompatActivity {
    Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        loginBtn = findViewById(R.id.btnLogin);
        registerBtn = findViewById(R.id.btnRegister);

        loginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(StartPageActivity.this, AuthActivity.class);
            intent.putExtra("auth_type", "login");
            startActivity(intent);
        });

        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(StartPageActivity.this, AuthActivity.class);
            intent.putExtra("auth_type", "register");
            startActivity(intent);
        });
    }
}
