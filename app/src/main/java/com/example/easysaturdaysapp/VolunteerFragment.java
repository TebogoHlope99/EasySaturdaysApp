package com.example.easysaturdaysapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
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
            holder.stats.setText("Sign up for a shift");

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

        // --- DISCLAIMER HEADER ---
        android.widget.TextView disclaimerTitle = new android.widget.TextView(getContext());
        disclaimerTitle.setText("⚠️ Disclaimer");
        disclaimerTitle.setTextColor(android.graphics.Color.RED);
        disclaimerTitle.setTextSize(16f);
        disclaimerTitle.setTypeface(null, android.graphics.Typeface.ITALIC);
        disclaimerTitle.setGravity(android.view.Gravity.CENTER);
        disclaimerTitle.setPadding(0, 0, 0, 5);
        layout.addView(disclaimerTitle);

        // --- DISCLAIMER TEXT ---
        android.widget.TextView disclaimerText = new android.widget.TextView(getContext());
        // UPDATED: Clear text about today's date and 24-hour cancellation
        disclaimerText.setText("You must book a shift at least 1 day in advance (no same-day bookings). You cannot cancel a shift less than 24 hours before it starts.");
        disclaimerText.setTextColor(android.graphics.Color.DKGRAY);
        disclaimerText.setTextSize(14f);
        disclaimerText.setTypeface(null, Typeface.BOLD);
        disclaimerText.setGravity(android.view.Gravity.CENTER);
        disclaimerText.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        disclaimerText.setPadding(0, 0, 0, 20);
        layout.addView(disclaimerText);

        // 1. Date Picker Button
        final android.widget.Button btnDate = new android.widget.Button(getContext());
        btnDate.setText("Select Date");
        final String[] selectedDate = {""};
        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            // FIX: Prevent today and past dates. Set min date to TOMORROW.
            c.add(Calendar.DAY_OF_MONTH, 1);

            DatePickerDialog dpd = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                // Format to yyyy-MM-dd for C#
                selectedDate[0] = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnDate.setText("Date: " + selectedDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            // Disable past dates and today
            dpd.getDatePicker().setMinDate(c.getTimeInMillis());
            dpd.show();
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
                        showStyledPopup("✅ Success", "Shift Signed Up!", android.graphics.Color.parseColor("#10B981"));
                    } else {
                        String errorMsg = "Failed to sign up";
                        try {
                            String rawError = response.errorBody().string();
                            org.json.JSONObject json = new org.json.JSONObject(rawError);
                            errorMsg = json.optString("message", "Failed to sign up");
                        } catch (Exception e) {
                            errorMsg = "Failed: " + response.code();
                        }
                        showStyledPopup("⚠️ Error", errorMsg, android.graphics.Color.parseColor("#EF4444"));
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

    private void showStyledPopup(String title, String message, int titleColor) {
        if (getContext() == null) return;

        // 1. Create a centered TextView for the message
        TextView messageView = new TextView(getContext());
        messageView.setText(message);
        messageView.setTextSize(16f);
        messageView.setGravity(android.view.Gravity.CENTER);
        messageView.setTextColor(android.graphics.Color.DKGRAY);

        // Add padding so it doesn't touch the edges
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        messageView.setPadding(padding, padding / 2, padding, padding / 2);

        // 2. Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setView(messageView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // 3. Show the dialog and style the title/button
        AlertDialog dialog = builder.create();
        dialog.show();

        // Style the title color after showing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#1E3A8A"));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
    }
}