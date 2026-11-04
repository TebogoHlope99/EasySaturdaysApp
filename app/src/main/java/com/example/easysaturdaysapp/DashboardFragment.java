package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

                        if (data.upcomingShifts != null) {
                            tvUpcoming.setText(String.valueOf(data.upcomingShifts.size()));
                            // Load shifts into RecyclerView
                            shiftsRecyclerView.setAdapter(new ShiftAdapter(data.upcomingShifts));
                        } else {
                            tvUpcoming.setText("0");
                        }
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

            // LONG PRESS TO CANCEL SHIFT
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Cancel Shift")
                        .setMessage("Are you sure you want to cancel your shift for " + s.campaignTitle + "? (You cannot cancel less than 24 hours before)")
                        .setPositiveButton("Yes, Cancel", (d, w) -> {
                            CancelShiftRequest req = new CancelShiftRequest();
                            req.userId = User.currentUser.id;
                            req.shiftId = s.id;

                            ApiClient.getRetrofitInstance().create(ApiService.class).cancelShift(req).enqueue(new Callback<ApiResponse>() {
                                @Override
                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                    if(response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Shift Cancelled Successfully", Toast.LENGTH_SHORT).show();
                                        // Refresh the fragment to update the list
                                        getParentFragmentManager().beginTransaction().detach(DashboardFragment.this).attach(DashboardFragment.this).commit();
                                    } else {
                                        // Try to parse the error message from the backend
                                        try {
                                            String errorBody = response.errorBody().string();
                                            org.json.JSONObject json = new org.json.JSONObject(errorBody);
                                            String cleanMessage = json.getString("message");
                                            Toast.makeText(getContext(), cleanMessage, Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), "Cannot cancel: Less than 24 hours", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<ApiResponse> call, Throwable t) {
                                    Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            });
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