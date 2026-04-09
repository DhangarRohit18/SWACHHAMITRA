package com.example.civsetu.report;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import com.example.civsetu.R;
import com.example.civsetu.databinding.ActivityIssueSubmissionBinding;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import android.media.MediaRecorder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.text.Editable;
import android.text.TextWatcher;
import com.example.civsetu.utils.AICategorizer;

public class IssueSubmissionActivity extends AppCompatActivity {
    private ActivityIssueSubmissionBinding binding;
    private IssueViewModel viewModel;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private List<File> capturedImages = new ArrayList<>();
    private File voiceFile;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    private double currentLat = 0.0;
    private double currentLon = 0.0;
    private com.google.android.gms.maps.GoogleMap previewMap;
    private com.google.android.gms.maps.model.Marker currentMarker;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = com.example.civsetu.utils.PreferenceManager.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.example.civsetu.utils.LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIssueSubmissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupUI();
        setupObservers();
        setupMapPreview();

        if (allPermissionsGranted()) {
            startCamera();
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_FINE_LOCATION },
                    10);
        }
    }

    private void setupMapPreview() {
        com.google.android.gms.maps.SupportMapFragment mapFragment = (com.google.android.gms.maps.SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                previewMap = googleMap;
                previewMap.getUiSettings().setAllGesturesEnabled(false);
            });
        }
    }

    private void updateMapMarker(double lat, double lon) {
        if (previewMap == null)
            return;
        com.google.android.gms.maps.model.LatLng pos = new com.google.android.gms.maps.model.LatLng(lat, lon);
        if (currentMarker == null) {
            currentMarker = previewMap.addMarker(
                    new com.google.android.gms.maps.model.MarkerOptions().position(pos).title("Report Location"));
        } else {
            currentMarker.setPosition(pos);
        }
        previewMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(pos, 15f));
    }

    private void setupUI() {
        String[] categories = getResources().getStringArray(R.array.categories_array);
        binding.autoCategory
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories));

        binding.btnCapture.setOnClickListener(v -> takePhoto());
        binding.btnRecordVoice.setOnClickListener(v -> toggleRecording());
        binding.btnSubmit.setOnClickListener(v -> submitIssue());

        TextWatcher aiWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String title = binding.etTitle.getText().toString();
                String desc = binding.etDescription.getText().toString();
                String suggestion = AICategorizer.suggestCategory(title, desc);
                if (!suggestion.equals("General")) {
                    binding.autoCategory.setText(suggestion, false);
                    binding.tvAISuggestion.setVisibility(View.VISIBLE);
                    binding.tvAISuggestion.setText(getString(R.string.ai_suggested_category, suggestion));
                } else {
                    binding.tvAISuggestion.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.etTitle.addTextChangedListener(aiWatcher);
        binding.etDescription.addTextChangedListener(aiWatcher);
    }

    private void setupObservers() {
        viewModel.getSubmissionResult().observe(this, issue -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.issue_submitted_successfully, Toast.LENGTH_LONG).show();

            // Redirect to Map with highlight
            Intent intent = new Intent(this, com.example.civsetu.map.MapActivity.class);
            intent.putExtra("LAT", currentLat);
            intent.putExtra("LON", currentLon);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        viewModel.getErrorMessage().observe(this, error -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null)
            return;
        File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        capturedImages.add(photoFile);
                        binding.tvImageCount.setText(getString(R.string.images_captured, capturedImages.size()));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(IssueSubmissionActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
            binding.btnRecordVoice.setText(R.string.stop_recording);
        } else {
            stopRecording();
            binding.btnRecordVoice.setText(R.string.record_voice);
        }
        isRecording = !isRecording;
    }

    private void startRecording() {
        voiceFile = new File(getExternalFilesDir(null), "voice_" + System.currentTimeMillis() + ".m4a");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(voiceFile.getAbsolutePath());
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLon = location.getLongitude();
                        binding.tvCoordinates.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f",
                                currentLat, currentLon));
                        updateAddress(currentLat, currentLon);
                        updateMapMarker(currentLat, currentLon);
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateAddress(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                binding.etAddress.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void submitIssue() {
        String title = binding.etTitle.getText().toString();
        String desc = binding.etDescription.getText().toString();
        String category = binding.autoCategory.getText().toString();
        String address = binding.etAddress.getText().toString();

        if (title.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Title and Category are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String englishCategory = getEnglishCategory(category);

        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.submitIssue(title, desc, englishCategory, address, currentLat, currentLon, capturedImages, voiceFile);
    }

    private String getEnglishCategory(String localizedCategory) {
        return com.example.civsetu.utils.CategoryUtils.getEnglishCategory(this, localizedCategory);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
