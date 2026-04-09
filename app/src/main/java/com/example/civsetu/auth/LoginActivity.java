package com.example.civsetu.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.civsetu.MainActivity;
import com.example.civsetu.databinding.ActivityLoginBinding;
import com.example.civsetu.utils.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int OFFLINE_REQ_CODE = 101;
    private static final String SMS_TARGET_NUMBER = "7385819857";

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = com.example.civsetu.utils.PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupObservers();

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Static Credentials for Testing & Demo
            String staticRole = null;
            String staticDept = null;

            if (pass.equals("admin123")) {
                staticRole = "Official";
                if (email.equals("admin@civsetu.com")) staticDept = "General";
                else if (email.equals("roads@civsetu.com")) staticDept = "Roads";
                else if (email.equals("water@civsetu.com")) staticDept = "Water Supply";
                else if (email.equals("waste@civsetu.com")) staticDept = "Waste Management";
                else if (email.equals("electricity@civsetu.com")) staticDept = "Electricity";
                else if (email.equals("health@civsetu.com")) staticDept = "Public Health";
                else if (email.equals("potholes@civsetu.com")) staticDept = "Potholes";
                else if (email.equals("garbage@civsetu.com")) staticDept = "Garbage";
                else if (email.equals("streetlights@civsetu.com")) staticDept = "Street Lights";
                else if (email.equals("drainage@civsetu.com")) staticDept = "Drainage";
                else if (email.equals("official@civsetu.com")) staticDept = "General";
            } else if (email.equals("citizen@civsetu.com") && pass.equals("citizen123")) {
                staticRole = "Citizen";
                staticDept = "General";
            }

            if (staticRole != null) {
                // Perform Mock Login
                PreferenceManager.getInstance(this).saveRole(staticRole);
                PreferenceManager.getInstance(this).saveDepartment(staticDept);
                PreferenceManager.getInstance(this).saveEmail(email);
                
                Toast.makeText(this, "Login Successful (" + staticRole + ")", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                // Real Firebase Authentication for other users
                PreferenceManager.getInstance(this).saveDepartment("General"); 
                PreferenceManager.getInstance(this).saveRole("Citizen");
                binding.progressBar.setVisibility(View.VISIBLE);
                viewModel.login(email, pass);
            }
        });

        binding.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.btnOfflineSms.setOnClickListener(v -> checkOfflinePermissions());
    }

    private void checkOfflinePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS, 
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, OFFLINE_REQ_CODE);
        } else {
            showOfflineReportDialog();
        }
    }

    private void showOfflineReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Offline SMS Report");
        builder.setMessage("Reporting to: " + SMS_TARGET_NUMBER);
        
        final EditText input = new EditText(this);
        input.setHint("Enter issue description...");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        lp.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(lp);
        
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Send SMS", (dialog, which) -> {
            String description = input.getText().toString().trim();
            if (!description.isEmpty()) {
                fetchLocationAndSendSms(description);
            } else {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void fetchLocationAndSendSms(String description) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sendSms(description, "Loc: Permission Denied");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            String locStr;
            if (location != null) {
                locStr = "Loc: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
            } else {
                locStr = "Loc: Unavailable (Enable GPS)";
            }
            sendSms(description, locStr);
        }).addOnFailureListener(e -> {
            sendSms(description, "Loc: Error fetching");
        });
    }

    private void sendSms(String description, String locationInfo) {
        try {
            String message = "CivSetu Report:\n" + description + "\n" + locationInfo;
            SmsManager smsManager = SmsManager.getDefault();
            // Split message if it exceeds 160 characters
            java.util.ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(SMS_TARGET_NUMBER, null, parts, null, null);
            Toast.makeText(this, "Offline Report Sent via SMS", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == OFFLINE_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showOfflineReportDialog();
            } else {
                Toast.makeText(this, "Permissions needed for offline reporting", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupObservers() {
        viewModel.getAuthResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            String email = binding.etEmail.getText().toString().trim();
            PreferenceManager.getInstance(this).saveEmail(email);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        viewModel.getErrorMessage().observe(this, error -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
}
