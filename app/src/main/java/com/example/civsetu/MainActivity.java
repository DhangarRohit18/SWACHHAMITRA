package com.example.civsetu;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.civsetu.auth.LoginActivity;
import com.example.civsetu.dashboard.AdminDashboardActivity;
import com.example.civsetu.dashboard.CitizenDashboardActivity;
import com.example.civsetu.dashboard.OfficialDashboardActivity;
import com.example.civsetu.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in (either via Firebase or static mock)
        boolean isMockLogin = PreferenceManager.getInstance(this).getEmail() != null;
        if (FirebaseAuth.getInstance().getCurrentUser() == null && !isMockLogin) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String role = PreferenceManager.getInstance(this).getRole();
        String dept = PreferenceManager.getInstance(this).getDepartment();

        if ("Official".equalsIgnoreCase(role)) {
            if ("General".equalsIgnoreCase(dept)) {
                // Admin Account -> Map Command Center
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                // Departmental Account -> List/Task Management
                startActivity(new Intent(this, OfficialDashboardActivity.class));
            }
        } else {
            // Citizen Account -> Reporting & Personal Dashboard
            startActivity(new Intent(this, CitizenDashboardActivity.class));
        }

        finish();
    }
}
