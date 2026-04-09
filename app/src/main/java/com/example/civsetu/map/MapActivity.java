package com.example.civsetu.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.civsetu.R;
import com.example.civsetu.databinding.ActivityMapBinding;
import com.example.civsetu.model.Issue;
import com.example.civsetu.report.IssueViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityMapBinding binding;
    private GoogleMap mMap;
    private ClusterManager<IssueClusterItem> mClusterManager;
    private HeatmapTileProvider mHeatmapProvider;
    private TileOverlay mHeatmapOverlay;
    private IssueViewModel viewModel;
    private BottomSheetBehavior bottomSheetBehavior;
    private boolean isHeatmapMode = false;
    private Marker highlightMarker;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = com.example.civsetu.utils.PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.getRoot());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        binding.btnToggleHeatmap.setOnClickListener(v -> toggleHeatmap());
        binding.btnToggleHeatmap.setText(getString(R.string.show_heatmap));
    }

    private void toggleHeatmap() {
        isHeatmapMode = !isHeatmapMode;
        mMap.clear();
        highlightMarker = null; // Clear highlight marker on toggle

        if (isHeatmapMode) {
            binding.btnToggleHeatmap.setText(getString(R.string.show_clusters));
            List<Issue> issues = viewModel.getAllIssues().getValue();
            if (issues != null)
                updateHeatmap(issues);
        } else {
            binding.btnToggleHeatmap.setText(getString(R.string.show_heatmap));
            setupClusterManager();
            List<Issue> issues = viewModel.getAllIssues().getValue();
            if (issues != null)
                updateClusters(issues);

            // Re-apply highlight if needed
            checkAndApplyHighlight();
        }
    }

    private void updateHeatmap(List<Issue> issues) {
        if (issues == null || issues.isEmpty())
            return;
        List<LatLng> list = new ArrayList<>();
        for (Issue issue : issues) {
            list.add(new LatLng(issue.getLatitude(), issue.getLongitude()));
        }
        mHeatmapProvider = new HeatmapTileProvider.Builder().data(list).build();
        mHeatmapOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatmapProvider));
    }

    private void updateClusters(List<Issue> issues) {
        if (issues == null || mClusterManager == null)
            return;
        mClusterManager.clearItems();
        for (Issue issue : issues) {
            mClusterManager.addItem(new IssueClusterItem(issue));
        }
        mClusterManager.cluster();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Apply Premium Map Style
        try {
            boolean success = mMap.setMapStyle(
                    com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
            if (!success) {
                Log.e("MapActivity", "Style parsing failed.");
            }
        } catch (android.content.res.Resources.NotFoundException e) {
            Log.e("MapActivity", "Can't find style. Error: ", e);
        }

        setupClusterManager();
        setupObservers();
        viewModel.fetchAllIssues();

        checkAndApplyHighlight();
    }

    private void setupClusterManager() {
        mClusterManager = new ClusterManager<>(this, mMap);

        // Custom Renderer for Colored Markers
        mClusterManager
                .setRenderer(new com.google.maps.android.clustering.view.DefaultClusterRenderer<IssueClusterItem>(this,
                        mMap, mClusterManager) {
                    @Override
                    protected void onBeforeClusterItemRendered(@NonNull IssueClusterItem item,
                            @NonNull MarkerOptions markerOptions) {
                        float hue = BitmapDescriptorFactory.HUE_GREEN;
                        String priority = item.getIssue().getPriority();
                        if (priority != null) {
                            switch (priority.toLowerCase()) {
                                case "critical":
                                    hue = BitmapDescriptorFactory.HUE_RED;
                                    break;
                                case "high":
                                    hue = BitmapDescriptorFactory.HUE_ORANGE;
                                    break;
                                case "medium":
                                    hue = BitmapDescriptorFactory.HUE_BLUE;
                                    break;
                            }
                        }
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
                    }
                });

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(item -> {
            showIssueDetails(item.getIssue());
            return true;
        });
    }

    private void checkAndApplyHighlight() {
        if (getIntent().hasExtra("LAT") && getIntent().hasExtra("LON")) {
            double lat = getIntent().getDoubleExtra("LAT", 0.0);
            double lon = getIntent().getDoubleExtra("LON", 0.0);
            LatLng pos = new LatLng(lat, lon);

            // Move camera
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));

            // Add a temporary highlight marker
            if (highlightMarker != null)
                highlightMarker.remove();
            highlightMarker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("Newly Reported Issue")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            if (highlightMarker != null)
                highlightMarker.showInfoWindow();
        }
    }

    private void setupObservers() {
        viewModel.getAllIssues().observe(this, issues -> {
            if (issues != null) {
                if (isHeatmapMode) {
                    updateHeatmap(issues);
                } else {
                    updateClusters(issues);
                }
            }
        });
    }

    private void showIssueDetails(Issue issue) {
        binding.bottomSheet.tvSheetTitle.setText(issue.getTitle());
        binding.bottomSheet.tvSheetDescription.setText(issue.getDescription());
        binding.bottomSheet.tvSheetStatus.setText(issue.getStatus());

        binding.bottomSheet.btnViewFullDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.civsetu.report.IssueDetailActivity.class);
            intent.putExtra("ISSUE_ID", issue.getId());
            startActivity(intent);
        });

        binding.bottomSheet.btnNavigate.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + issue.getLatitude() + "," + issue.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
