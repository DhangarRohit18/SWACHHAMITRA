package com.example.civsetu.report;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.civsetu.R;
import com.example.civsetu.databinding.ActivityIssueDetailBinding;
import com.example.civsetu.model.Issue;
import com.example.civsetu.utils.PreferenceManager;
import com.example.civsetu.utils.SLAUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class IssueDetailActivity extends AppCompatActivity {
    private ActivityIssueDetailBinding binding;
    private IssueViewModel viewModel;
    private String issueId;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        issueId = getIntent().getStringExtra("ISSUE_ID");
        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);

        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());

        if (issueId != null) {
            viewModel.getIssueDetails(issueId).observe(this, this::updateUI);
        }

        binding.btnUpvote.setOnClickListener(v -> {
            if (issueId != null) {
                viewModel.upvoteIssue(issueId);
                Toast.makeText(this, R.string.upvoted_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Issue issue) {
        if (issue == null)
            return;

        binding.tvDetailTitle.setText(issue.getTitle());
        binding.tvDetailDescription.setText(issue.getDescription());
        binding.tvDetailCategory
                .setText(com.example.civsetu.utils.CategoryUtils.getLocalizedCategory(this, issue.getCategory()));
        binding.tvDetailAddress.setText(issue.getAddress());
        binding.chipDetailStatus.setText(issue.getStatus());
        binding.tvUpvoteCount.setText(String.valueOf(issue.getUpvotes()));

        String priority = issue.getPriority() != null ? issue.getPriority() : "Medium";
        binding.chipDetailPriority.setText(priority.toUpperCase());
        setPriorityStyle(priority);

        // SLA
        String slaStatus = SLAUtils.getSLAStatus(new Date(issue.getCreatedAt()), priority);
        binding.chipSLA.setText(getString(R.string.sla_status) + ": " + slaStatus.toUpperCase());
        int slaColor = SLAUtils.getSLAStatusColor(slaStatus);
        binding.chipSLA.setChipBackgroundColor(ColorStateList.valueOf(slaColor));
        binding.chipSLA.setTextColor(Color.WHITE);

        // AI Reasoning
        if (issue.getAiReasoning() != null && !issue.getAiReasoning().isEmpty()) {
            binding.aiInsightCard.setVisibility(View.VISIBLE);
            binding.tvAiReasoning.setText(issue.getAiReasoning());
        } else {
            binding.aiInsightCard.setVisibility(View.GONE);
        }

        // Voice Note
        if (issue.getVoiceUrl() != null && issue.getVoiceUrl().startsWith("data:audio")) {
            binding.voiceCard.setVisibility(View.VISIBLE);
            binding.btnPlayVoice.setOnClickListener(v -> toggleVoicePlayback(issue.getVoiceUrl()));
        } else {
            binding.voiceCard.setVisibility(View.GONE);
        }

        // Images
        if (issue.getImageUrls() != null && !issue.getImageUrls().isEmpty()) {
            binding.ivDetailIssue.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(issue.getImageUrls().get(0))
                    .into(binding.ivDetailIssue);
        }

        binding.btnDetailNavigate.setOnClickListener(v -> {
            String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f",
                    issue.getLatitude(), issue.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // Admin/Official Actions Logic
        String userDept = PreferenceManager.getInstance(this).getDepartment();
        boolean isAdmin = "General".equalsIgnoreCase(userDept);
        boolean isOfficial = userDept != null && !isAdmin && issue.getCategory() != null
                && issue.getCategory().equalsIgnoreCase(userDept);

        if (isAdmin && "Pending".equalsIgnoreCase(issue.getStatus())) {
            binding.layoutAdminActions.setVisibility(View.VISIBLE);
            binding.btnApproveDetail.setVisibility(View.VISIBLE);
            binding.btnDeclineDetail.setVisibility(View.VISIBLE);
            binding.btnResolveDetail.setVisibility(View.GONE);
        } else if (isOfficial && "In Progress".equalsIgnoreCase(issue.getStatus())) {
            binding.layoutAdminActions.setVisibility(View.VISIBLE);
            binding.btnApproveDetail.setVisibility(View.GONE);
            binding.btnDeclineDetail.setVisibility(View.GONE);
            binding.btnResolveDetail.setVisibility(View.VISIBLE);
        } else {
            binding.layoutAdminActions.setVisibility(View.GONE);
        }

        binding.btnApproveDetail.setOnClickListener(v -> {
            String remarks = String.format(getString(R.string.remarks_admin_approved), issue.getCategory());
            viewModel.updateIssueStatus(issue.getId(), "In Progress", remarks);
            Toast.makeText(this, R.string.issue_approved_assigned, Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnDeclineDetail.setOnClickListener(v -> {
            viewModel.updateIssueStatus(issue.getId(), "Rejected", getString(R.string.remarks_admin_declined));
            Toast.makeText(this, R.string.issue_declined, Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnResolveDetail.setOnClickListener(v -> {
            viewModel.updateIssueStatus(issue.getId(), "Resolved", getString(R.string.remarks_official_resolved));
            Toast.makeText(this, R.string.issue_resolved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setPriorityStyle(String priority) {
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
        binding.chipDetailPriority.setChipBackgroundColor(ColorStateList.valueOf(color));
    }

    private void toggleVoicePlayback(String base64Audio) {
        if (isPlaying) {
            stopPlayback();
        } else {
            startPlayback(base64Audio);
        }
    }

    private void startPlayback(String base64Audio) {
        try {
            String pureBase64 = base64Audio.substring(base64Audio.indexOf(",") + 1);
            byte[] audioBytes = Base64.decode(pureBase64, Base64.DEFAULT);

            File tempFile = File.createTempFile("playback", ".m4a", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(audioBytes);
            fos.close();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            binding.btnPlayVoice.setImageResource(android.R.drawable.ic_media_pause);

            mediaPlayer.setOnCompletionListener(mp -> stopPlayback());

        } catch (IOException e) {
            Log.e("IssueDetail", "Playback failed", e);
            Toast.makeText(this, R.string.audio_playback_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        binding.btnPlayVoice.setImageResource(android.R.drawable.ic_media_play);
        binding.voicePlaybackProgress.setProgress(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayback();
    }
}
