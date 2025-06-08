package com.example.idrive.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.idrive.R;
import com.example.idrive.fragments.LoginFragment;
import com.example.idrive.fragments.RegisterFragment;

public class AuthActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            String authType = getIntent().getStringExtra("auth_type");
            Fragment fragment = "register".equals(authType) ? new RegisterFragment() : new LoginFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcvAuth, fragment)
                    .commit();
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fcvAuth, fragment)
                .commit();
    }
}
