package com.example.civsetu.sustainability;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.civsetu.databinding.ActivitySustainabilityBinding;
import com.example.civsetu.model.User;
import com.example.civsetu.utils.PreferenceManager;
import java.util.ArrayList;

public class SustainabilityActivity extends AppCompatActivity {
    private ActivitySustainabilityBinding binding;
    private SustainabilityViewModel viewModel;
    private LeaderboardAdapter adapter;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySustainabilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SustainabilityViewModel.class);
        setupRecyclerView();
        setupObservers();

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // Fetch data
        viewModel.fetchLeaderboard();
        
        // Mocking user stats from intent or local storage
        int userPoints = getIntent().getIntExtra("USER_POINTS", 0);
        binding.tvGreenPoints.setText(String.valueOf(userPoints));
        
        // Cleanliness score formula (mocked for now based on points/activity)
        int score = calculateCleanlinessScore(userPoints);
        binding.tvScore.setText(String.valueOf(score));
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(new ArrayList<>());
        binding.rvTopLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTopLeaderboard.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getLeaderboard().observe(this, users -> {
            if (users != null) {
                adapter.setLeaderboard(users);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateCleanlinessScore(int points) {
        // Simple mock logic for score 0-100
        if (points > 2000) return 98;
        if (points > 1000) return 85;
        if (points > 500) return 72;
        return 45;
    }
}
