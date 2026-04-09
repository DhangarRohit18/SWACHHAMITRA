package com.example.civsetu.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.civsetu.MainActivity;
import com.example.civsetu.R;
import com.example.civsetu.databinding.ActivityAdminDashboardBinding;
import com.example.civsetu.map.IssueClusterItem;
import com.example.civsetu.model.Issue;
import com.example.civsetu.report.IssueDetailActivity;
import com.example.civsetu.report.IssueViewModel;
import com.example.civsetu.utils.PreferenceManager;
import com.example.civsetu.utils.SLAUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.clustering.ClusterManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityAdminDashboardBinding binding;
    private GoogleMap mMap;
    private ClusterManager<IssueClusterItem> mClusterManager;
    private IssueViewModel viewModel;
    private List<Issue> allIssues = new ArrayList<>();

    private String currentCategory = "All Issues";
    private String currentStatus = "All";
    private String currentSearch = "";
    private boolean filterSLABreached = false;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);

        setupToolbar();
        setupFilters();
        setupMap();
        setupObservers();

        binding.fabListView.setOnClickListener(v -> {
            Intent intent = new Intent(this, OfficialDashboardActivity.class);
            startActivity(intent);
        });
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

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        PreferenceManager.getInstance(this).clear();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.cgCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipAllIssues)
                    currentCategory = "All Issues";
                else if (id == R.id.chipRoads)
                    currentCategory = "Roads";
                else if (id == R.id.chipWater)
                    currentCategory = "Water Supply";
                else if (id == R.id.chipElectricity)
                    currentCategory = "Electricity";
                else if (id == R.id.chipWaste)
                    currentCategory = "Waste Management";
                else if (id == R.id.chipPotholes)
                    currentCategory = "Potholes";
                else if (id == R.id.chipGarbage)
                    currentCategory = "Garbage";
                else if (id == R.id.chipOther)
                    currentCategory = "Other";
                applyFilters();
            }
        });

        binding.cgStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.statusAll)
                    currentStatus = "All";
                else if (id == R.id.statusPending)
                    currentStatus = "Pending";
                else if (id == R.id.statusInProgress)
                    currentStatus = "In Progress";
                else if (id == R.id.statusResolved)
                    currentStatus = "Resolved";
                else if (id == R.id.statusEscalated)
                    currentStatus = "Escalated";
                else if (id == R.id.statusRejected)
                    currentStatus = "Rejected";
                applyFilters();
            }
        });

        binding.chipSLABreached.setOnClickListener(v -> {
            filterSLABreached = !filterSLABreached;
            binding.chipSLABreached.setChecked(filterSLABreached);
            applyFilters();
        });

        binding.toggleMapType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && mMap != null) {
                if (checkedId == R.id.btnMap)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else if (checkedId == R.id.btnSatellite)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void setupObservers() {
        viewModel.getAllIssues().observe(this, issues -> {
            if (issues != null) {
                this.allIssues = issues;
                applyFilters();
            }
        });
        viewModel.fetchAllIssues();
    }

    private void applyFilters() {
        if (mMap == null || mClusterManager == null)
            return;

        List<Issue> filtered = new ArrayList<>();
        for (Issue issue : allIssues) {
            boolean matchesCategory = currentCategory.equals("All Issues")
                    || issue.getCategory().contains(currentCategory);
            boolean matchesStatus = currentStatus.equals("All") || issue.getStatus().equalsIgnoreCase(currentStatus);
            boolean matchesSearch = issue.getTitle().toLowerCase().contains(currentSearch) ||
                    issue.getDescription().toLowerCase().contains(currentSearch);

            boolean isSLAExceeded = false;
            if (filterSLABreached) {
                String priority = issue.getPriority() != null ? issue.getPriority() : "Medium";
                String slaStatus = SLAUtils.getSLAStatus(new Date(issue.getCreatedAt()), priority);
                isSLAExceeded = SLAUtils.STATUS_BREACHED.equalsIgnoreCase(slaStatus);
            }

            if (matchesCategory && matchesStatus && matchesSearch && (!filterSLABreached || isSLAExceeded)) {
                filtered.add(issue);
            }
        }

        updateMapMarkers(filtered);
    }

    private void updateMapMarkers(List<Issue> issues) {
        mClusterManager.clearItems();
        for (Issue issue : issues) {
            mClusterManager.addItem(new IssueClusterItem(issue));
        }
        mClusterManager.cluster();

        if (!issues.isEmpty()) {
            LatLng first = new LatLng(issues.get(0).getLatitude(), issues.get(0).getLongitude());
            // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(first, 12));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Apply Premium Map Style
        try {
            boolean success = mMap.setMapStyle(
                    com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
            if (!success) {
                android.util.Log.e("AdminDashboard", "Style parsing failed.");
            }
        } catch (android.content.res.Resources.NotFoundException e) {
            android.util.Log.e("AdminDashboard", "Can't find style. Error: ", e);
        }

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(item -> {
            Intent intent = new Intent(this, IssueDetailActivity.class);
            intent.putExtra("ISSUE_ID", item.getIssue().getId());
            startActivity(intent);
            return true;
        });

        applyFilters();
    }
}
