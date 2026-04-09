package com.example.civsetu.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.civsetu.databinding.ActivityCategoryInsightBinding;
import com.example.civsetu.databinding.ItemCategoryStatBinding;
import com.example.civsetu.model.Issue;
import com.example.civsetu.report.IssueViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryInsightActivity extends AppCompatActivity {
    private ActivityCategoryInsightBinding binding;
    private IssueViewModel viewModel;
    private CategoryStatAdapter adapter;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = com.example.civsetu.utils.PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryInsightBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        
        setupToolbar();
        setupRecyclerView();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CategoryStatAdapter(new ArrayList<>());
        binding.rvCategoryStats.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategoryStats.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getAllIssues().observe(this, issues -> {
            Map<String, CategoryStat> statsMap = new HashMap<>();
            
            // Define all categories
            String[] commonCategories = { "Roads", "Water Supply", "Electricity", "Waste Management", "Public Health", "Other" };
            for (String cat : commonCategories) {
                statsMap.put(cat, new CategoryStat(cat));
            }

            for (Issue issue : issues) {
                String cat = issue.getCategory() != null ? issue.getCategory() : "Other";
                CategoryStat stat = statsMap.get(cat);
                if (stat == null) {
                    stat = new CategoryStat(cat);
                    statsMap.put(cat, stat);
                }
                stat.total++;
                if ("Resolved".equalsIgnoreCase(issue.getStatus())) {
                    stat.resolved++;
                } else if ("Pending".equalsIgnoreCase(issue.getStatus())) {
                    stat.pending++;
                }
            }

            adapter.setStats(new ArrayList<>(statsMap.values()));
        });
    }

    static class CategoryStat {
        String name;
        int total;
        int resolved;
        int pending;

        CategoryStat(String name) {
            this.name = name;
        }
    }

    static class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {
        private List<CategoryStat> stats;

        CategoryStatAdapter(List<CategoryStat> stats) {
            this.stats = stats;
        }

        void setStats(List<CategoryStat> stats) {
            this.stats = stats;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCategoryStatBinding binding = ItemCategoryStatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryStat stat = stats.get(position);
            holder.binding.tvCategoryTitle.setText(stat.name);
            holder.binding.tvTotalCount.setText(String.valueOf(stat.total));
            holder.binding.tvResolved.setText(stat.resolved + " Resolved");
            holder.binding.tvPending.setText(stat.pending + " Pending");
            
            if (stat.total > 0) {
                int progress = (stat.resolved * 100) / stat.total;
                holder.binding.progressResolution.setProgress(progress);
            } else {
                holder.binding.progressResolution.setProgress(0);
            }
        }

        @Override
        public int getItemCount() {
            return stats.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemCategoryStatBinding binding;
            ViewHolder(ItemCategoryStatBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
