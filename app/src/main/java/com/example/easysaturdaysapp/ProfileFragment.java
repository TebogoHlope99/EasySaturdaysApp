package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        EditText etFirst = view.findViewById(R.id.etProfFirstName);
        EditText etLast = view.findViewById(R.id.etProfLastName);
        EditText etEmail = view.findViewById(R.id.etProfEmail);
        EditText etPhone = view.findViewById(R.id.etProfPhone);
        EditText etSkills = view.findViewById(R.id.etProfSkills);
        Button btnSave = view.findViewById(R.id.btnSaveProfile);
        Button btnCancel = view.findViewById(R.id.btnCancelProfile);

        if(User.currentUser != null) {
            User u = User.currentUser;
            etFirst.setText(u.firstName);
            etLast.setText(u.lastName);
            etEmail.setText(u.email);
            etPhone.setText(u.phone);
            etSkills.setText(u.skills);
        }

        btnSave.setOnClickListener(v -> {
            User updateUser = new User();
            updateUser.id = User.currentUser.id;
            updateUser.firstName = etFirst.getText().toString();
            updateUser.lastName = etLast.getText().toString();
            updateUser.phone = etPhone.getText().toString();
            updateUser.skills = etSkills.getText().toString();

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.updateProfile(updateUser).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                        User.currentUser.firstName = updateUser.firstName;
                        User.currentUser.lastName = updateUser.lastName;
                        User.currentUser.phone = updateUser.phone;
                        User.currentUser.skills = updateUser.skills;
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error updating", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> {
            // Go back to Dashboard
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        });

        return view;
    }
}