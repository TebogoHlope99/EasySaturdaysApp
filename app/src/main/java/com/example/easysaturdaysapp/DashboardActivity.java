package com.example.easysaturdaysapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    // We store the logged in user's ID here. In a real app, use secure storage.
    public static int currentUserId = 0;
    public static User currentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvTotalDonated = findViewById(R.id.tvTotalDonated);
        TextView tvCampaigns = findViewById(R.id.tvCampaignsSupported);
        TextView tvHours = findViewById(R.id.tvHours);
        TextView tvUpcoming = findViewById(R.id.tvUpcomingShifts);

        if(currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.firstName + "!");

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.getDashboard(currentUserId).enqueue(new Callback<DashboardData>() {
                @Override
                public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        DashboardData data = response.body();
                        tvTotalDonated.setText("R" + (int)data.totalDonated);
                        tvCampaigns.setText(String.valueOf(data.campaignsSupported));
                        tvHours.setText(String.valueOf(data.hoursContributed));
                        tvUpcoming.setText(String.valueOf(data.upcomingShifts));
                    }
                }
                @Override
                public void onFailure(Call<DashboardData> call, Throwable t) {
                    Toast.makeText(DashboardActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}