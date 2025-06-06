package com.example.idrive.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.idrive.R;
import com.example.idrive.fragments.LoginFragment;
import com.example.idrive.fragments.RegisterFragment;

public class AuthActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        String authType = getIntent().getStringExtra("auth_type");
        if ("register".equals(authType)) {
            loadFragment(new RegisterFragment());
        } else {
            loadFragment(new LoginFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcvAuth, fragment);
        transaction.commit();
    }
}
