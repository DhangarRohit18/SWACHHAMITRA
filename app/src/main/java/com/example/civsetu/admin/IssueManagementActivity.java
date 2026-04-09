package com.example.civsetu.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.civsetu.databinding.ActivityIssueManagementBinding;
import com.example.civsetu.model.Issue;
import com.example.civsetu.report.IssueViewModel;
import java.util.List;

public class IssueManagementActivity extends AppCompatActivity {
    private ActivityIssueManagementBinding binding;
    private IssueViewModel viewModel;
    private String issueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIssueManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        issueId = getIntent().getStringExtra("ISSUE_ID");

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        String[] statuses = { "Pending", "In Progress", "Resolved", "Rejected" };
        binding.autoStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));

        binding.btnUpdate.setOnClickListener(v -> {
            String status = binding.autoStatus.getText().toString();
            String remarks = binding.etRemarks.getText().toString();
            if (!status.isEmpty()) {
                binding.progressBar.setVisibility(View.VISIBLE);
                viewModel.updateIssueStatus(issueId, status, remarks);
                Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupObservers() {
        if (issueId != null) {
            viewModel.getIssueDetails(issueId).observe(this, this::populateIssueData);
        }
    }

    private void populateIssueData(Issue issue) {
        if (issue == null) return;

        binding.tvTitle.setText(issue.getTitle());
        binding.tvDescription.setText(issue.getDescription());
        binding.autoStatus.setText(issue.getStatus(), false);

        List<String> images = issue.getImageUrls();
        binding.imageContainer.removeAllViews();
        if (images != null && !images.isEmpty()) {
            for (String url : images) {
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) (150 * getResources().getDisplayMetrics().density),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 16, 0);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                
                Glide.with(this).load(url).into(imageView);
                binding.imageContainer.addView(imageView);
            }
        }
    }
}
