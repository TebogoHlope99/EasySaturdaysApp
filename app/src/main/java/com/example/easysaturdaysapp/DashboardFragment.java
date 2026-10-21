package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvTotalDonated = view.findViewById(R.id.tvTotalDonated);
        TextView tvCampaigns = view.findViewById(R.id.tvCampaignsSupported);
        TextView tvHours = view.findViewById(R.id.tvHours);
        TextView tvUpcoming = view.findViewById(R.id.tvUpcomingShifts);
        RecyclerView shiftsRecyclerView = view.findViewById(R.id.shiftsRecyclerView);

        shiftsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (User.currentUser != null) {
            tvWelcome.setText("Welcome, " + User.currentUser.firstName + "!");

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.getDashboard(User.currentUser.id).enqueue(new Callback<DashboardData>() {
                @Override
                public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DashboardData data = response.body();
                        tvTotalDonated.setText("R" + (int)data.totalDonated);
                        tvCampaigns.setText(String.valueOf(data.campaignsSupported));
                        tvHours.setText(String.valueOf(data.hoursContributed));
                        tvUpcoming.setText(String.valueOf(data.upcomingShifts.size()));

                        // Load shifts into RecyclerView
                        shiftsRecyclerView.setAdapter(new ShiftAdapter(data.upcomingShifts));
                    }
                }
                @Override
                public void onFailure(Call<DashboardData> call, Throwable t) {
                    Toast.makeText(getContext(), "Failed to load stats", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    // --- SHIFT ADAPTER ---
    class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.VH> {
        List<DashboardData.UpcomingShift> list;
        ShiftAdapter(List<DashboardData.UpcomingShift> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            DashboardData.UpcomingShift s = list.get(position);
            holder.text1.setText(s.campaignTitle);
            holder.text2.setText(s.date + " | " + s.time);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}