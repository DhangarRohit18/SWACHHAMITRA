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
import com.example.civsetu.databinding.ActivityOfficialDashboardBinding;
import com.example.civsetu.databinding.ItemIssueBinding;
import com.example.civsetu.model.Issue;
import com.example.civsetu.report.IssueDetailActivity;
import com.example.civsetu.report.IssueViewModel;
import com.example.civsetu.utils.PreferenceManager;
import com.example.civsetu.utils.SLAUtils;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OfficialDashboardActivity extends AppCompatActivity {
    private ActivityOfficialDashboardBinding binding;
    private IssueViewModel viewModel;
    private OfficialIssueAdapter adapter;
    private List<Issue> allIssues = new ArrayList<>();
    private String currentSearch = "";
    private String currentFilter = "All";
    private String userDept = "General";
    private boolean isAdmin = false;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficialDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        userDept = PreferenceManager.getInstance(this).getDepartment();
        if (userDept == null)
            userDept = "General";

        isAdmin = "General".equalsIgnoreCase(userDept);

        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupObservers();
        setupRefresh();

        updateTitle();

        binding.btnCategoryInsights
                .setOnClickListener(v -> startActivity(new Intent(this, CategoryInsightActivity.class)));

        binding.btnMapView.setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));
    }

    private void setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.fetchAllIssues();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                filterIssues();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "All";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipAll)
                    currentFilter = "All";
                else if (id == R.id.chipPending)
                    currentFilter = "Pending";
                else if (id == R.id.chipInProgress)
                    currentFilter = "In Progress";
                else if (id == R.id.chipResolved)
                    currentFilter = "Resolved";
                else if (id == R.id.chipRejected)
                    currentFilter = "Rejected";
                else if (id == R.id.chipCritical)
                    currentFilter = "Critical";
            }
            filterIssues();
        });
    }

    private void filterIssues() {
        List<Issue> filteredList = new ArrayList<>();
        int pending = 0;
        for (Issue issue : allIssues) {
            boolean matchesDept;
            if (isAdmin) {
                matchesDept = true;
            } else {
                // Officials see all issues for their department, but can only act on "In
                // Progress" ones
                matchesDept = issue.getCategory().equalsIgnoreCase(userDept);
            }

            if ("Pending".equalsIgnoreCase(issue.getStatus()))
                pending++;

            boolean matchesSearch = (issue.getTitle() != null && issue.getTitle().toLowerCase().contains(currentSearch))
                    ||
                    (issue.getDescription() != null && issue.getDescription().toLowerCase().contains(currentSearch));

            boolean matchesFilter = currentFilter.equalsIgnoreCase("All") ||
                    issue.getStatus().equalsIgnoreCase(currentFilter) ||
                    (currentFilter.equalsIgnoreCase("Critical") && "Critical".equalsIgnoreCase(issue.getPriority()));

            if (matchesSearch && matchesFilter && matchesDept) {
                filteredList.add(issue);
            }
        }

        // Quick Access logic: Show newest issue if available
        if (!filteredList.isEmpty()) {
            Issue newest = filteredList.get(0);
            binding.cardQuickAccess.setVisibility(View.VISIBLE);
            binding.tvQuickTitle.setText(newest.getTitle());
            binding.tvQuickCategory.setText(newest.getCategory());
            binding.btnQuickView.setOnClickListener(v -> {
                Intent intent = new Intent(this, IssueDetailActivity.class);
                intent.putExtra("ISSUE_ID", newest.getId());
                startActivity(intent);
            });
        } else {
            binding.cardQuickAccess.setVisibility(View.GONE);
        }

        binding.tvPendingCount.setText(String.valueOf(pending));
        adapter.setIssues(filteredList);
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            } else if (item.getItemId() == R.id.action_language) {
                com.example.civsetu.utils.LanguageUtils.showLanguageDialog(this);
                return true;
            } else if (item.getItemId() == R.id.action_insights) {
                startActivity(new Intent(this, CategoryInsightActivity.class));
                return true;
            }
            return false;
        });
    }

    private void updateTitle() {
        if (!isAdmin) {
            String localizedDept = com.example.civsetu.utils.CategoryUtils.getLocalizedCategory(this, userDept);
            binding.toolbar.setTitle(localizedDept + " " + getString(R.string.dashboard));
        } else {
            binding.toolbar.setTitle(getString(R.string.admin_command_center));
        }
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
        adapter = new OfficialIssueAdapter(new ArrayList<>(), isAdmin,
                new OfficialIssueAdapter.OnIssueActionListener() {
                    @Override
                    public void onIssueClick(Issue issue) {
                        Intent intent = new Intent(OfficialDashboardActivity.this, IssueDetailActivity.class);
                        intent.putExtra("ISSUE_ID", issue.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onApprove(Issue issue) {
                        String remarks = String.format(getString(R.string.remarks_admin_approved), issue.getCategory());
                        viewModel.updateIssueStatus(issue.getId(), "In Progress", remarks);
                        Toast.makeText(OfficialDashboardActivity.this, R.string.issue_approved_assigned,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDecline(Issue issue) {
                        viewModel.updateIssueStatus(issue.getId(), "Rejected",
                                getString(R.string.remarks_admin_declined));
                        Toast.makeText(OfficialDashboardActivity.this, R.string.issue_declined, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onResolve(Issue issue) {
                        viewModel.updateIssueStatus(issue.getId(), "Resolved",
                                getString(R.string.remarks_official_resolved));
                        Toast.makeText(OfficialDashboardActivity.this, R.string.issue_resolved, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        binding.rvAllIssues.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllIssues.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getAllIssues().observe(this, issues -> {
            this.allIssues = issues;
            filterIssues();
        });
    }

    static class OfficialIssueAdapter extends RecyclerView.Adapter<OfficialIssueAdapter.ViewHolder> {
        private List<Issue> issues;
        private OnIssueActionListener listener;
        private boolean isAdmin;

        interface OnIssueActionListener {
            void onIssueClick(Issue issue);

            void onApprove(Issue issue);

            void onDecline(Issue issue);

            void onResolve(Issue issue);
        }

        OfficialIssueAdapter(List<Issue> issues, boolean isAdmin, OnIssueActionListener listener) {
            this.issues = issues;
            this.isAdmin = isAdmin;
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
            holder.binding.tvCategory.setText(com.example.civsetu.utils.CategoryUtils
                    .getLocalizedCategory(holder.itemView.getContext(), issue.getCategory()));
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

            if (isAdmin) {
                if ("Pending".equalsIgnoreCase(issue.getStatus())) {
                    holder.binding.layoutActions.setVisibility(View.VISIBLE);
                    holder.binding.btnApprove.setVisibility(View.VISIBLE);
                    holder.binding.btnApprove.setText(R.string.approve_assign);
                    holder.binding.btnDecline.setVisibility(View.VISIBLE);
                    holder.binding.btnResolve.setVisibility(View.GONE);
                } else {
                    holder.binding.layoutActions.setVisibility(View.GONE);
                }
            } else {
                if ("In Progress".equalsIgnoreCase(issue.getStatus())) {
                    holder.binding.layoutActions.setVisibility(View.VISIBLE);
                    holder.binding.btnApprove.setVisibility(View.GONE);
                    holder.binding.btnDecline.setVisibility(View.GONE);
                    holder.binding.btnResolve.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.layoutActions.setVisibility(View.GONE);
                }
            }

            holder.binding.btnApprove.setOnClickListener(v -> listener.onApprove(issue));
            holder.binding.btnDecline.setOnClickListener(v -> listener.onDecline(issue));
            holder.binding.btnResolve.setOnClickListener(v -> listener.onResolve(issue));

            holder.getRoot().setOnClickListener(v -> listener.onIssueClick(issue));
        }

        private void setPriorityStyle(ViewHolder holder, String priority) {
            int color;
            switch (priority.toLowerCase()) {
                case "critical":
                    color = Color.parseColor("#FFDADA");
                    break;
                case "high":
                    color = Color.parseColor("#FFE4D1");
                    break;
                case "medium":
                    color = Color.parseColor("#D1E4FF");
                    break;
                default:
                    color = Color.parseColor("#E1E1E1");
                    break;
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

            ViewGroup getRoot() {
                return (ViewGroup) binding.getRoot();
            }
        }
    }
}
