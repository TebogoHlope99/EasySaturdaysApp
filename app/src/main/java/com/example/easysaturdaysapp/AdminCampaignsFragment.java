package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class AdminCampaignsFragment extends Fragment {
    RecyclerView recyclerView;
    AdminCampaignAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_campaigns, container, false);

        recyclerView = view.findViewById(R.id.adminCampaignRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Existing Create Campaign Button
        view.findViewById(R.id.btnCreateCampaign).setOnClickListener(v -> showCreateDialog());

        // NEW: Admin Request Button
        view.findViewById(R.id.btnRequestAdminAction).setOnClickListener(v -> showAdminRequestDialog());

        loadCampaigns();
        return view;
    }

    // --- ADMIN REQUEST DIALOG ---
    private void showAdminRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🛡️ Request Admin Action");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Dropdown to select Create or Delete
        TextView label = new TextView(getContext());
        label.setText("Select Action:");
        layout.addView(label);

        android.widget.Spinner actionSpinner = new android.widget.Spinner(getContext());
        String[] actions = {"CreateAdmin", "DeleteAdmin"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, actions);
        actionSpinner.setAdapter(adapter);
        layout.addView(actionSpinner);

        // Input for Email (only used for Create)
        TextView label2 = new TextView(getContext());
        label2.setText("\nNew Admin Email (leave blank for Delete):");
        layout.addView(label2);

        EditText etEmail = new EditText(getContext());
        etEmail.setHint("e.g. newadmin@demo.com");
        layout.addView(etEmail);

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            AdminRequest req = new AdminRequest();
            req.actionType = actionSpinner.getSelectedItem().toString();

            if(req.actionType.equals("CreateAdmin")) {
                req.newAdminEmail = etEmail.getText().toString();
                if(req.newAdminEmail.isEmpty()) {
                    Toast.makeText(getContext(), "Email required for Create", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // For delete, we would normally show a dropdown of admins.
                // For simplicity in this demo, we'll hardcode ID 4 as an example.
                req.targetAdminId = "4";
            }

            ApiClient.getRetrofitInstance().create(ApiService.class).createAdminRequest(req).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getContext(), "Request sent to Super Admin!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to send request", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // --- CREATE DIALOG ---
    private void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create Campaign");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText etTitle = new EditText(getContext()); etTitle.setHint("Title"); layout.addView(etTitle);
        EditText etDesc = new EditText(getContext()); etDesc.setHint("Description"); layout.addView(etDesc);
        EditText etGoal = new EditText(getContext()); etGoal.setHint("Goal Amount (R)"); etGoal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(etGoal);

        builder.setView(layout);
        builder.setPositiveButton("Create", (dialog, which) -> {
            Campaign c = new Campaign();
            c.title = etTitle.getText().toString();
            c.description = etDesc.getText().toString();
            c.goalAmount = Double.parseDouble(etGoal.getText().toString());

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.createCampaign(c).enqueue(new Callback<ApiResponse>() {
                @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) { loadCampaigns(); }
                @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
            });
        });
        builder.show();
    }

    // --- EDIT DIALOG ---
    private void showEditDialog(Campaign campaign) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Campaign");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText etTitle = new EditText(getContext());
        etTitle.setText(campaign.title); // Pre-fill with existing title
        layout.addView(etTitle);

        EditText etDesc = new EditText(getContext());
        etDesc.setText(campaign.description); // Pre-fill
        layout.addView(etDesc);

        EditText etGoal = new EditText(getContext());
        etGoal.setText(String.valueOf((int)campaign.goalAmount)); // Pre-fill
        etGoal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etGoal);

        builder.setView(layout);
        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            Campaign edited = new Campaign();
            edited.id = campaign.id;
            edited.title = etTitle.getText().toString();
            edited.description = etDesc.getText().toString();
            edited.goalAmount = Double.parseDouble(etGoal.getText().toString());
            edited.raisedAmount = campaign.raisedAmount; // Keep the existing raised amount

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.editCampaign(edited).enqueue(new Callback<ApiResponse>() {
                @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getContext(), "Campaign Updated!", Toast.LENGTH_SHORT).show();
                        loadCampaigns();
                    }
                }
                @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadCampaigns() {
        ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
        api.getCampaigns().enqueue(new Callback<List<Campaign>>() {
            @Override public void onResponse(Call<List<Campaign>> call, Response<List<Campaign>> response) {
                if(response.isSuccessful()) {
                    adapter = new AdminCampaignAdapter(response.body());
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override public void onFailure(Call<List<Campaign>> call, Throwable t) {}
        });
    }

    // --- RECYCLER ADAPTER ---
    class AdminCampaignAdapter extends RecyclerView.Adapter<AdminCampaignAdapter.VH> {
        List<Campaign> list;
        AdminCampaignAdapter(List<Campaign> list) { this.list = list; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            Campaign c = list.get(position);
            holder.text1.setText(c.title);
            holder.text2.setText("Raised: R" + (int)c.raisedAmount + " / Goal: R" + (int)c.goalAmount);

            // LONG PRESS shows the Edit/Delete Menu
            holder.itemView.setOnLongClickListener(v -> {
                String[] options = {"✏️ Edit", "🗑️ Delete"};
                new AlertDialog.Builder(getContext())
                        .setTitle(c.title)
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                // Edit selected
                                showEditDialog(c);
                            } else {
                                // Delete selected
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Delete Campaign")
                                        .setMessage("Are you sure you want to delete this campaign?")
                                        .setPositiveButton("Yes", (d, w) -> {
                                            ApiClient.getRetrofitInstance().create(ApiService.class).deleteCampaign(c.id).enqueue(new Callback<ApiResponse>() {
                                                @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                                    Toast.makeText(getContext(), "Campaign Deleted", Toast.LENGTH_SHORT).show();
                                                    loadCampaigns();
                                                }
                                                @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
                                            });
                                        })
                                        .setNegativeButton("No", null).show();
                            }
                        })
                        .show();
                return true;
            });
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) { super(v); text1 = v.findViewById(android.R.id.text1); text2 = v.findViewById(android.R.id.text2); }
        }
    }
}