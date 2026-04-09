package com.example.civsetu.report;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraHelper {
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    public CameraHelper() {
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void startCamera(Context context, LifecycleOwner lifecycleOwner, PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void takePhoto(Context context, OnImageSavedCallback callback) {
        if (imageCapture == null) return;

        File outputDirectory = context.getExternalCacheDir();
        File photoFile = new File(outputDirectory, new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                callback.onSuccess(photoFile);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                callback.onError(exception);
            }
        });
    }

    public void shutdown() {
        cameraExecutor.shutdown();
    }

    public interface OnImageSavedCallback {
        void onSuccess(File file);
        void onError(Exception e);
    }
}
