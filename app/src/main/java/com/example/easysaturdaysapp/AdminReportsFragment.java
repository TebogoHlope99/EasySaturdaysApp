package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_reports, container, false);

        TextView tvUsers = view.findViewById(R.id.tvRepUsers);
        TextView tvDonations = view.findViewById(R.id.tvRepDonations);
        TextView tvShifts = view.findViewById(R.id.tvRepShifts);
        TextView tvCampaigns = view.findViewById(R.id.tvRepCampaigns);

        ApiClient.getRetrofitInstance().create(ApiService.class).getReports().enqueue(new Callback<AdminReport>() {
            @Override public void onResponse(Call<AdminReport> call, Response<AdminReport> response) {
                if(response.isSuccessful() && response.body() != null) {
                    AdminReport r = response.body();
                    tvUsers.setText("Total Users: " + r.totalUsers + " (Donors: " + r.totalDonors + ", Volunteers: " + r.totalVolunteers + ")");
                    tvDonations.setText("Total Donations: R" + (int)r.totalDonations + " (Avg: R" + (int)r.avgDonation + ")");
                    tvShifts.setText("Total Volunteer Shifts: " + r.totalShifts);
                    tvCampaigns.setText("Active Campaigns: " + r.activeCampaigns);
                }
            }
            @Override public void onFailure(Call<AdminReport> call, Throwable t) {}
        });
        return view;
    }
}