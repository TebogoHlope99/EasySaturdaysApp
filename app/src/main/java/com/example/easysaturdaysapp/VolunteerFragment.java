package com.example.easysaturdaysapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VolunteerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.volunteerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
        api.getCampaigns().enqueue(new Callback<List<Campaign>>() {
            @Override
            public void onResponse(Call<List<Campaign>> call, Response<List<Campaign>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new VolunteerAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Campaign>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load campaigns", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VH> {
        List<Campaign> list;
        VolunteerAdapter(List<Campaign> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Campaign c = list.get(position);
            holder.title.setText(c.title);
            holder.desc.setText(c.description);
            holder.stats.setText("Sign up for a shift"); // Custom text

            holder.itemView.setOnClickListener(v -> showShiftDialog(c));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, desc, stats;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvTitle);
                desc = v.findViewById(R.id.tvDescription);
                stats = v.findViewById(R.id.tvStats);
            }
        }
    }

    private void showShiftDialog(Campaign campaign) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Volunteer for " + campaign.title);

        // Create a vertical layout for the dialog inputs
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // DISCLAIMER
        android.widget.TextView disclaimer = new android.widget.TextView(getContext());
        disclaimer.setText("⚠️ Warning: You cannot cancel a shift less than 24 hours before it starts.");
        disclaimer.setTextColor(android.graphics.Color.RED);
        disclaimer.setPadding(0, 0, 0, 20);
        layout.addView(disclaimer);

        // 1. Date Picker Button
        final android.widget.Button btnDate = new android.widget.Button(getContext());
        btnDate.setText("Select Date");
        final String[] selectedDate = {""};
        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                // Format to yyyy-MM-dd for C#
                selectedDate[0] = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnDate.setText("Date: " + selectedDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnDate);

        // 2. Time Spinner
        Spinner timeSpinner = new Spinner(getContext());
        String[] times = {"08:00 - 12:00", "13:00 - 17:00"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, times);
        timeSpinner.setAdapter(adapter);
        layout.addView(timeSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Sign Up", (dialog, which) -> {
            if(selectedDate[0].isEmpty()) {
                Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            ShiftRequest request = new ShiftRequest();
            request.userId = User.currentUser.id;
            request.campaignId = campaign.id;
            request.date = selectedDate[0];
            request.time = timeSpinner.getSelectedItem().toString();

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.signUpShift(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getContext(), "Shift Signed Up!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Show error from C# (e.g., "You already have a shift on this day")
                        try {
                            String errorMsg = response.errorBody().string();
                            Toast.makeText(getContext(), "Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to sign up", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}