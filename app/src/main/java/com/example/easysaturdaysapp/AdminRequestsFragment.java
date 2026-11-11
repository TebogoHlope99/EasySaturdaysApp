package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRequestsFragment extends Fragment {
    Spinner spinnerAdmins;
    List<AdminUser> adminList = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;
    List<String> adminNames = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_requests, container, false);

        EditText etNewAdminEmail = view.findViewById(R.id.etNewAdminEmail);
        Button btnRequestCreate = view.findViewById(R.id.btnRequestCreate);
        spinnerAdmins = view.findViewById(R.id.spinnerAdmins);
        Button btnRequestDelete = view.findViewById(R.id.btnRequestDelete);

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, adminNames);
        spinnerAdmins.setAdapter(spinnerAdapter);

        loadAdminsIntoSpinner();

        // --- CREATE ADMIN REQUEST ---
        btnRequestCreate.setOnClickListener(v -> {
            String email = etNewAdminEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }

            AdminRequest req = new AdminRequest();
            req.requestedByUserId = User.currentUser.id;
            req.actionType = "CreateAdmin";
            req.newAdminEmail = email;

            sendRequest(req);
            etNewAdminEmail.setText("");
        });

        // --- DELETE ADMIN REQUEST ---
        btnRequestDelete.setOnClickListener(v -> {
            if (adminList.isEmpty()) {
                Toast.makeText(getContext(), "No admins available to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = spinnerAdmins.getSelectedItemPosition();
            AdminUser selectedAdmin = adminList.get(selectedIndex);

            AdminRequest req = new AdminRequest();
            req.requestedByUserId = User.currentUser.id;
            req.actionType = "DeleteAdmin";
            req.targetAdminId = String.valueOf(selectedAdmin.id);

            sendRequest(req);
        });

        return view;
    }

    private void sendRequest(AdminRequest req) {
        ApiClient.getRetrofitInstance().create(ApiService.class).createAdminRequest(req).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Request sent to Super Admin!", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        // Print the exact error from C#
                        String rawError = response.errorBody().string();
                        Toast.makeText(getContext(), "Server says: " + rawError, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed with Code: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdminsIntoSpinner() {
        ApiClient.getRetrofitInstance().create(ApiService.class).getAllUsers().enqueue(new Callback<List<AdminUser>>() {
            @Override
            public void onResponse(Call<List<AdminUser>> call, Response<List<AdminUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adminList.clear();
                    adminNames.clear();

                    for (AdminUser u : response.body()) {
                        if (u.role != null && u.role.equalsIgnoreCase("Admin")) {
                            adminList.add(u);
                            adminNames.add(u.firstName + " " + u.lastName + " (" + u.email + ")");
                        }
                    }
                    spinnerAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<AdminUser>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load admins", Toast.LENGTH_SHORT).show();
            }
        });
    }
}