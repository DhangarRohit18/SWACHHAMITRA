package com.example.civsetu.dashboard;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.chip.Chip;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.civsetu.MainActivity;
import com.example.civsetu.R;
import com.example.civsetu.databinding.ActivityCitizenDashboardBinding;
import com.example.civsetu.databinding.ItemIssueBinding;
import com.example.civsetu.map.MapActivity;
import com.example.civsetu.model.Issue;
import com.example.civsetu.report.IssueDetailActivity;
import com.example.civsetu.report.IssueSubmissionActivity;
import com.example.civsetu.report.IssueViewModel;
import com.example.civsetu.sustainability.SustainabilityActivity;
import com.example.civsetu.surveys.SurveyActivity;
import com.example.civsetu.utils.PreferenceManager;
import com.example.civsetu.utils.SLAUtils;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CitizenDashboardActivity extends AppCompatActivity {
    private ActivityCitizenDashboardBinding binding;
    private IssueViewModel viewModel;
    private IssueAdapter adapter;
    private List<Issue> allIssues = new ArrayList<>();
    private String currentSearch = "";
    private String currentStatusFilter = "All";

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCitizenDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupObservers();
        setupRefresh();

        binding.fabReport.setOnClickListener(v -> startActivity(new Intent(this, IssueSubmissionActivity.class)));
        binding.cardMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        binding.btnSurveys.setOnClickListener(v -> startActivity(new Intent(this, SurveyActivity.class)));
        
        // Sustainability Hub Link
        binding.tvUserPoints.setOnClickListener(v -> {
            Intent intent = new Intent(this, SustainabilityActivity.class);
            intent.putExtra("USER_POINTS", parsePoints(binding.tvUserPoints.getText().toString()));
            startActivity(intent);
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.fetchUserIssues();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                filterIssues();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatusFilter = "All";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) currentStatusFilter = "All";
                else if (id == R.id.chipPending) currentStatusFilter = "Pending";
                else if (id == R.id.chipInProgress) currentStatusFilter = "In Progress";
                else if (id == R.id.chipResolved) currentStatusFilter = "Resolved";
            }
            filterIssues();
        });
    }

    private void filterIssues() {
        List<Issue> filteredList = new ArrayList<>();
        int resolvedCount = 0;
        int pendingCount = 0;

        for (Issue issue : allIssues) {
            if ("Resolved".equalsIgnoreCase(issue.getStatus())) resolvedCount++;
            else if ("Pending".equalsIgnoreCase(issue.getStatus())) pendingCount++;

            boolean matchesSearch = (issue.getTitle() != null && issue.getTitle().toLowerCase().contains(currentSearch)) ||
                    (issue.getDescription() != null && issue.getDescription().toLowerCase().contains(currentSearch));
            boolean matchesStatus = currentStatusFilter.equalsIgnoreCase("All") ||
                    (issue.getStatus() != null && issue.getStatus().equalsIgnoreCase(currentStatusFilter));

            if (matchesSearch && matchesStatus) {
                filteredList.add(issue);
            }
        }
        adapter.setIssues(filteredList);
        
        binding.tvTotalCount.setText(String.valueOf(allIssues.size()));
        binding.tvResolvedCount.setText(String.valueOf(resolvedCount));
        binding.tvPendingCount.setText(String.valueOf(pendingCount));
        
        // Automatic high-impact points calculation preview (from mock user profile)
        int calculatedPoints = (resolvedCount * 100) + (allIssues.size() * 50);
        binding.tvUserPoints.setText(calculatedPoints + " Points");
    }

    private int parsePoints(String pointsText) {
        try {
            return Integer.parseInt(pointsText.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            } else if (item.getItemId() == R.id.action_language) {
                com.example.civsetu.utils.LanguageUtils.showLanguageDialog(this);
                return true;
            }
            return false;
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        PreferenceManager.getInstance(this).clear();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        adapter = new IssueAdapter(new ArrayList<>(), issue -> {
            Intent intent = new Intent(this, IssueDetailActivity.class);
            intent.putExtra("ISSUE_ID", issue.getId());
            startActivity(intent);
        });
        binding.rvMyIssues.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyIssues.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getUserIssues().observe(this, issues -> {
            if (!this.allIssues.isEmpty() && issues.size() == this.allIssues.size()) {
                for (int i = 0; i < issues.size(); i++) {
                    Issue newIssue = issues.get(i);
                    for (Issue oldIssue : this.allIssues) {
                        if (newIssue.getId().equals(oldIssue.getId()) && !newIssue.getStatus().equals(oldIssue.getStatus())) {
                            com.example.civsetu.utils.NotificationHelper.showNotification(this, 
                                "Issue Status Updated", 
                                "Your issue '" + newIssue.getTitle() + "' is now " + newIssue.getStatus());
                        }
                    }
                }
            }
            this.allIssues = issues;
            filterIssues();
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null)
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    static class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {
        private List<Issue> issues;
        private OnIssueClickListener listener;

        interface OnIssueClickListener {
            void onIssueClick(Issue issue);
        }

        IssueAdapter(List<Issue> issues, OnIssueClickListener listener) {
            this.issues = issues;
            this.listener = listener;
        }

        void setIssues(List<Issue> issues) {
            this.issues = issues;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemIssueBinding binding = ItemIssueBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                    false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Issue issue = issues.get(position);
            holder.binding.tvTitle.setText(issue.getTitle());
            holder.binding.tvCategory.setText(com.example.civsetu.utils.CategoryUtils.getLocalizedCategory(holder.itemView.getContext(), issue.getCategory()));
            holder.binding.tvDescription.setText(issue.getDescription());
            holder.binding.chipStatus.setText(issue.getStatus());

            if (issue.getImageUrls() != null && !issue.getImageUrls().isEmpty()) {
                holder.binding.ivIssue.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(issue.getImageUrls().get(0))
                        .into(holder.binding.ivIssue);
            } else {
                holder.binding.ivIssue.setVisibility(View.GONE);
            }

            String priority = issue.getPriority() != null ? issue.getPriority() : "Medium";
            String slaStatus = SLAUtils.getSLAStatus(new Date(issue.getCreatedAt()), priority);
            
            holder.binding.chipSLA.setText(slaStatus.toUpperCase());
            int slaColor = SLAUtils.getSLAStatusColor(slaStatus);
            holder.binding.chipSLA.setChipBackgroundColor(ColorStateList.valueOf(slaColor));
            holder.binding.chipSLA.setTextColor(Color.WHITE);

            holder.binding.chipPriority.setText(priority.toUpperCase());
            setPriorityStyle(holder, priority);

            holder.itemView.setOnClickListener(v -> listener.onIssueClick(issue));
        }

        private void setPriorityStyle(ViewHolder holder, String priority) {
            int color;
            if (priority == null) priority = "medium";
            switch (priority.toLowerCase()) {
                case "critical": color = Color.parseColor("#FFDADA"); break;
                case "high": color = Color.parseColor("#FFE4D1"); break;
                case "medium": color = Color.parseColor("#D1E4FF"); break;
                default: color = Color.parseColor("#E1E1E1"); break;
            }
            holder.binding.chipPriority.setChipBackgroundColor(ColorStateList.valueOf(color));
        }

        @Override
        public int getItemCount() {
            return issues.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemIssueBinding binding;

            ViewHolder(ItemIssueBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
