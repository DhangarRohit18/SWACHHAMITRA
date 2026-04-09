package com.example.civsetu.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.civsetu.MainActivity;
import com.example.civsetu.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = com.example.civsetu.utils.PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            String name = binding.etName.getText().toString().trim();
            // Forced to Citizen for registration
            String role = "Citizen";

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            viewModel.register(email, pass, role);
        });

        binding.btnLogin.setOnClickListener(v -> finish());
    }

    private void setupObservers() {
        viewModel.getAuthResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        viewModel.getErrorMessage().observe(this, error -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
}
