package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        EditText etFirst = findViewById(R.id.etProfFirstName);
        EditText etLast = findViewById(R.id.etProfLastName);
        EditText etEmail = findViewById(R.id.etProfEmail);
        EditText etPhone = findViewById(R.id.etProfPhone);
        EditText etSkills = findViewById(R.id.etProfSkills);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        Button btnCancel = findViewById(R.id.btnCancelProfile);

        // Pre-fill fields
        if(DashboardActivity.currentUser != null) {
            User u = DashboardActivity.currentUser;
            etFirst.setText(u.firstName);
            etLast.setText(u.lastName);
            etEmail.setText(u.email);
            etPhone.setText(u.phone);
            etSkills.setText(u.skills);
        }

        btnSave.setOnClickListener(v -> {
            User updateUser = new User();
            updateUser.id = DashboardActivity.currentUserId;
            updateUser.firstName = etFirst.getText().toString();
            updateUser.lastName = etLast.getText().toString();
            updateUser.phone = etPhone.getText().toString();
            updateUser.skills = etSkills.getText().toString();

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.updateProfile(updateUser).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                        // Update local user object
                        DashboardActivity.currentUser.firstName = updateUser.firstName;
                        DashboardActivity.currentUser.lastName = updateUser.lastName;
                        DashboardActivity.currentUser.phone = updateUser.phone;
                        DashboardActivity.currentUser.skills = updateUser.skills;
                        finish();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "Error updating", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}