package com.happydental.kidsadventure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private boolean isLoginMode = true;

    private TextView tvFormTitle;
    private TextInputLayout tilParentName;
    private TextInputEditText etParentName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnSubmit;
    private TextView tvSwitchMode;
    private Button btnSkip;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        tilParentName = findViewById(R.id.tilParentName);
        etParentName = findViewById(R.id.etParentName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);
        btnSkip = findViewById(R.id.btnSkip);
        progressBar = findViewById(R.id.progressBar);

        tvSwitchMode.setOnClickListener(v -> toggleMode());

        btnSubmit.setOnClickListener(v -> handleSubmit());

        btnSkip.setOnClickListener(v -> {
            NetworkClient.getInstance().setCurrentUserId("offline_guest");
            navigateToDashboard();
        });
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvFormTitle.setText("Welcome Back!");
            tilParentName.setVisibility(View.GONE);
            btnSubmit.setText("Login");
            tvSwitchMode.setText("Don't have an account? Register");
        } else {
            tvFormTitle.setText("Create Account");
            tilParentName.setVisibility(View.VISIBLE);
            btnSubmit.setText("Register");
            tvSwitchMode.setText("Already have an account? Login");
        }
    }

    private void handleSubmit() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        if (isLoginMode) {
            NetworkClient.getInstance().login(email, password, new NetworkClient.Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                }

                @Override
                public void onError(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            String parentName = etParentName.getText().toString().trim();
            if (parentName.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            NetworkClient.getInstance().register(parentName, email, password, new NetworkClient.Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                }

                @Override
                public void onError(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
