package com.example.easysaturdaysapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnViewCampaigns = findViewById(R.id.btnViewCampaigns);
        Button btnLoginRegister = findViewById(R.id.btnLoginRegister);

        // Go to public Campaigns view
        btnViewCampaigns.setOnClickListener(v -> {
            // If they are logged in, send to main app. If not, just show them campaigns (we'll route to login for simplicity here)
            if(User.currentUser != null) {
                startActivity(new Intent(HomeActivity.this, MainDrawerActivity.class));
            } else {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });

        // Go to Login
        btnLoginRegister.setOnClickListener(v -> {
            if(User.currentUser != null) {
                startActivity(new Intent(HomeActivity.this, MainDrawerActivity.class));
            } else {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });
    }
}