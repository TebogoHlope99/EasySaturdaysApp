package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class AdminUsersFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_users, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.adminUsersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ApiClient.getRetrofitInstance().create(ApiService.class).getAllUsers().enqueue(new Callback<List<AdminUser>>() {
            @Override public void onResponse(Call<List<AdminUser>> call, Response<List<AdminUser>> response) {
                if(response.isSuccessful()) recyclerView.setAdapter(new UserAdapter(response.body()));
            }
            @Override public void onFailure(Call<List<AdminUser>> call, Throwable t) {}
        });
        return view;
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        List<AdminUser> list;
        UserAdapter(List<AdminUser> list) { this.list = list; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            AdminUser u = list.get(position);
            holder.text1.setText(u.firstName + " " + u.lastName + " (" + u.role + ")");
            holder.text2.setText(u.email);

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(getContext()).setTitle("Delete User").setMessage("Are you sure?").setPositiveButton("Yes", (d, w) -> {
                    ApiClient.getRetrofitInstance().create(ApiService.class).deleteUser(u.id).enqueue(new Callback<ApiResponse>() {
                        @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) { getParentFragmentManager().beginTransaction().detach(AdminUsersFragment.this).attach(AdminUsersFragment.this).commit(); }
                        @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
                }).setNegativeButton("No", null).show();
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