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

        // Only Create Campaign exists here now
        view.findViewById(R.id.btnCreateCampaign).setOnClickListener(v -> showCreateDialog());

        loadCampaigns();
        return view;
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
        etTitle.setText(campaign.title);
        layout.addView(etTitle);

        EditText etDesc = new EditText(getContext());
        etDesc.setText(campaign.description);
        layout.addView(etDesc);

        EditText etGoal = new EditText(getContext());
        etGoal.setText(String.valueOf((int)campaign.goalAmount));
        etGoal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etGoal);

        builder.setView(layout);
        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            Campaign edited = new Campaign();
            edited.id = campaign.id;
            edited.title = etTitle.getText().toString();
            edited.description = etDesc.getText().toString();
            edited.goalAmount = Double.parseDouble(etGoal.getText().toString());
            edited.raisedAmount = campaign.raisedAmount;

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

            holder.itemView.setOnLongClickListener(v -> {
                String[] options = {"✏️ Edit", "🗑️ Delete"};
                new AlertDialog.Builder(getContext())
                        .setTitle(c.title)
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                showEditDialog(c);
                            } else {
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