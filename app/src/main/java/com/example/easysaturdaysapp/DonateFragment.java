package com.example.easysaturdaysapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.UUID;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Donate to " + campaign.title);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Amount Input
        final EditText etAmount = new EditText(getContext());
        etAmount.setHint("Enter amount (Min R50)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        // 2. Payment Method Spinner
        final Spinner methodSpinner = new Spinner(getContext());
        String[] methods = {"Credit / Debit Card"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, methods);
        methodSpinner.setAdapter(adapter);
        layout.addView(methodSpinner);

        // 3. Dynamic Container
        final android.widget.LinearLayout dynamicContainer = new android.widget.LinearLayout(getContext());
        dynamicContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(dynamicContainer);

        // 4. Card Details
        final android.widget.LinearLayout cardLayout = new android.widget.LinearLayout(getContext());
        cardLayout.setOrientation(android.widget.LinearLayout.VERTICAL);

        final EditText etCardNumber = new EditText(getContext());
        etCardNumber.setHint("Card Number (16 digits)");
        etCardNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String text = s.toString().replaceAll("\\s", "");
                if (text.length() > 16) text = text.substring(0, 16);
                String formatted = "";
                for (int i = 0; i < text.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted += " ";
                    formatted += text.charAt(i);
                }
                if (!formatted.equals(s.toString())) {
                    etCardNumber.setText(formatted);
                    etCardNumber.setSelection(formatted.length());
                }
            }
        });
        cardLayout.addView(etCardNumber);

        final EditText etExpiry = new EditText(getContext());
        etExpiry.setHint("Expiry (MM/YY)");
        etExpiry.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etExpiry.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String text = s.toString().replaceAll("/", "");
                if (text.length() > 4) text = text.substring(0, 4);
                if (text.length() > 2) {
                    String formatted = text.substring(0, 2) + "/" + text.substring(2);
                    if (!formatted.equals(s.toString())) {
                        etExpiry.setText(formatted);
                        etExpiry.setSelection(formatted.length());
                    }
                }
            }
        });
        cardLayout.addView(etExpiry);

        final EditText etCvv = new EditText(getContext());
        etCvv.setHint("CVV (3 digits)");
        etCvv.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        cardLayout.addView(etCvv);

        dynamicContainer.addView(cardLayout);

        // 5. Handle Spinner Changes
        methodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dynamicContainer.removeAllViews();
                String selectedMethod = methods[position];

                if (selectedMethod.equals("Credit / Debit Card")) {
                    dynamicContainer.addView(cardLayout);
                }
                /*else if (selectedMethod.equals("Cash")) {
                    TextView tvCash = new TextView(getContext());
                    tvCash.setText("A Reference Code will be emailed to you. Take it to the EasySaturdays office to verify your cash donation.");
                    tvCash.setTextColor(android.graphics.Color.RED);
                    tvCash.setPadding(0, 20, 0, 20);
                    dynamicContainer.addView(tvCash);
                }*/
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setView(layout);

        // --- SUBMIT BUTTON ---
        builder.setPositiveButton("Donate", (dialog, which) -> {
            String amountStr = etAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedMethod = methodSpinner.getSelectedItem().toString();
            DonateRequest request = new DonateRequest();
            request.userId = User.currentUser.id;
            request.campaignId = campaign.id;
            request.amount = Double.parseDouble(amountStr);
            request.method = selectedMethod;

            // Validate Card inputs
            if (selectedMethod.equals("Credit / Debit Card")) {
                String cardNum = etCardNumber.getText().toString().replaceAll("\\s", "");
                String expiry = etExpiry.getText().toString();
                String cvv = etCvv.getText().toString();

                if (cardNum.length() != 16) { Toast.makeText(getContext(), "Card must be 16 digits", Toast.LENGTH_SHORT).show(); return; }
                if (expiry.length() != 5) { Toast.makeText(getContext(), "Expiry must be MM/YY", Toast.LENGTH_SHORT).show(); return; }
                if (cvv.length() < 3) { Toast.makeText(getContext(), "Invalid CVV", Toast.LENGTH_SHORT).show(); return; }

                request.cardNumber = cardNum;
                request.cardExpiry = expiry;
                request.cardCvv = cvv;
            }
            /*else if (selectedMethod.equals("Cash")) {
                request.reference = "ESF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            }*/

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.donate(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        showStyledPopup("✅ Donation Successful", response.body().message, android.graphics.Color.parseColor("#10B981"));
                    } else {
                        String errorMsg = "Failed to donate";
                        try {
                            String rawError = response.errorBody().string();
                            org.json.JSONObject json = new org.json.JSONObject(rawError);
                            errorMsg = json.optString("message", "Failed to donate");
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

    // --- CENTERED PROFESSIONAL POPUP ---
    private void showStyledPopup(String title, String message, int titleColor) {
        if (getContext() == null) return;

        TextView messageView = new TextView(getContext());
        messageView.setText(message);
        messageView.setTextSize(16f);
        messageView.setGravity(android.view.Gravity.CENTER);
        messageView.setTextColor(android.graphics.Color.DKGRAY);

        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        messageView.setPadding(padding, padding / 2, padding, padding / 2);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setView(messageView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            // Refresh the fragment so the campaign raised amount updates
            getParentFragmentManager().beginTransaction().detach(this).attach(this).commit();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#1E3A8A"));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
    }
}