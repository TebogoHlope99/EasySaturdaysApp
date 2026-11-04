package com.example.easysaturdaysapp;

import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuperAdminListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_super_admins, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.adminsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Show a loading message (optional)
        Toast.makeText(getContext(), "Loading admins...", Toast.LENGTH_SHORT).show();

        ApiClient.getRetrofitInstance().create(ApiService.class).getAllUsers().enqueue(new Callback<List<AdminUser>>() {
            @Override
            public void onResponse(Call<List<AdminUser>> call, Response<List<AdminUser>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<AdminUser> allUsers = response.body();
                    List<AdminUser> adminsOnly = new ArrayList<>();

                    // Loop through ALL users and filter out Donors/Volunteers
                    for(AdminUser u : allUsers) {
                        if(u.role != null && (u.role.equals("Admin") || u.role.equals("SuperAdmin"))) {
                            adminsOnly.add(u);
                        }
                    }

                    // If the list is empty after filtering, show a message
                    if(adminsOnly.isEmpty()) {
                        Toast.makeText(getContext(), "No admins found in the database.", Toast.LENGTH_LONG).show();
                    } else {
                        recyclerView.setAdapter(new AdminListAdapter(adminsOnly));
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load data (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<AdminUser>> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    class AdminListAdapter extends RecyclerView.Adapter<AdminListAdapter.VH> {
        List<AdminUser> list;
        AdminListAdapter(List<AdminUser> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_super_admin, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AdminUser u = list.get(position);
            holder.tvName.setText(u.firstName + " " + u.lastName);
            holder.tvEmail.setText(u.email);
            holder.tvRole.setText("Role: " + u.role);

            if(u.role.equals("SuperAdmin")) {
                holder.tvRole.setTextColor(Color.parseColor("#10B981")); // Green for Super Admin
            } else {
                holder.tvRole.setTextColor(Color.parseColor("#F59E0B")); // Orange for Admin
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvRole;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvAdminName);
                tvEmail = v.findViewById(R.id.tvAdminEmail);
                tvRole = v.findViewById(R.id.tvAdminRole);
            }
        }
    }
}