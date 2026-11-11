package com.example.easysaturdaysapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etFirst = findViewById(R.id.etRegFirstName);
        EditText etLast = findViewById(R.id.etRegLastName);
        EditText etEmail = findViewById(R.id.etRegEmail);
        EditText etPass = findViewById(R.id.etRegPassword);
        EditText etConfirmPass = findViewById(R.id.etRegConfirmPassword);
        EditText etPhone = findViewById(R.id.etRegPhone);
        Spinner spinnerRole = findViewById(R.id.spinnerRole);
        CheckBox cbPopia = findViewById(R.id.cbPopia);
        CheckBox cbTerms = findViewById(R.id.cbTerms);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Setup Role Spinner
        String[] roles = {"Donor", "Volunteer", "Both"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirst.getText().toString().trim();
            String lastName = etLast.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString();
            String confirmPassword = etConfirmPass.getText().toString();

            // --- VALIDATION LOGIC ---

            // 1. Check empty fields
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_LONG).show();
                return;
            }

            // 2. Validate Email Pattern
            String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
            if (!email.matches(emailPattern)) {
                Toast.makeText(this, "Invalid email format. Example: @mail.com", Toast.LENGTH_LONG).show();
                return;
            }

            // 3. Check if passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
                return;
            }

            // 4. Check password complexity (8+ chars, 1 uppercase, 1 number)
            if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
                Toast.makeText(this, "Password must be at least 8 characters, contain 1 uppercase letter, and 1 number.", Toast.LENGTH_LONG).show();
                return;
            }

            // 5. Check Checkboxes (POPIA & Terms)
            if (!cbPopia.isChecked() || !cbTerms.isChecked()) {
                Toast.makeText(this, "You must consent to POPIA and the Code of Conduct.", Toast.LENGTH_LONG).show();
                return;
            }

            // --- ALL VALIDATIONS PASSED, SEND TO API ---
            User user = new User();
            user.firstName = firstName;
            user.lastName = lastName;
            user.email = email;
            user.password = password;
            user.confirmPassword = confirmPassword;
            user.phone = etPhone.getText().toString();
            user.role = spinnerRole.getSelectedItem().toString();
            user.skills = "General Support";
            user.popia = cbPopia.isChecked();
            user.terms = cbTerms.isChecked();

            ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);
            api.register(user).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Try to parse the C# error message
                        try {
                            String errorBody = response.errorBody().string();
                            org.json.JSONObject json = new org.json.JSONObject(errorBody);
                            String msg = json.optString("message", "Error registering account");
                            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "Error registering account", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }
}