package com.example.easysaturdaysapp;

import android.app.AlertDialog;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donate, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.donateRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
        api.getCampaigns().enqueue(new Callback<List<Campaign>>() {
            @Override
            public void onResponse(Call<List<Campaign>> call, Response<List<Campaign>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new DonateAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Campaign>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load campaigns", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    class DonateAdapter extends RecyclerView.Adapter<DonateAdapter.VH> {
        List<Campaign> list;
        DonateAdapter(List<Campaign> list) { this.list = list; }

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
            holder.stats.setText("Raised: R" + (int)c.raisedAmount + " / Goal: R" + (int)c.goalAmount);

            holder.itemView.setOnClickListener(v -> showDonateDialog(c));
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

    private void showDonateDialog(Campaign campaign) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Donate to " + campaign.title);

        // Create a vertical layout for the dialog inputs
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Amount Input
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter amount (Min R50)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(input);

        // 2. Payment Method Spinner
        Spinner methodSpinner = new Spinner(getContext());
        String[] methods = {"Credit Card", "Instant EFT", "Mobile Money", "Cash Drop-off"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, methods);
        methodSpinner.setAdapter(adapter);
        layout.addView(methodSpinner);

        // 3. Card Number Input (Hidden if Cash)
        final android.widget.EditText cardInput = new android.widget.EditText(getContext());
        cardInput.setHint("Card Number (16 digits)");
        cardInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(cardInput);

        // Toggle card visibility based on method
        methodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if(methods[position].equals("Cash Drop-off")) {
                    cardInput.setVisibility(View.GONE);
                } else {
                    cardInput.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        builder.setView(layout);

        builder.setPositiveButton("Donate", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if(amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            DonateRequest request = new DonateRequest();
            request.userId = User.currentUser.id;
            request.campaignId = campaign.id;
            request.amount = Double.parseDouble(amountStr);
            request.method = methodSpinner.getSelectedItem().toString();
            request.cardNumber = cardInput.getText().toString();

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.donate(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getContext(), "Donation Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String errorMsg = response.errorBody().string();
                            Toast.makeText(getContext(), "Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to donate", Toast.LENGTH_SHORT).show();
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