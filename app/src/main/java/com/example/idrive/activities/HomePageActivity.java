package com.example.idrive.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.idrive.R;
import com.example.idrive.fragments.AdminHomeFragment;
import com.example.idrive.fragments.MemberHomeFragment;

public class HomePageActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            boolean isAdmin = getIntent().getBooleanExtra("is_admin", false);
            Fragment fragment = isAdmin ? new AdminHomeFragment() : new MemberHomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcvHome, fragment)
                    .commit();
        }
    }
}
