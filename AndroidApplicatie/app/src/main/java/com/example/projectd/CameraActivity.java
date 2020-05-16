package com.example.projectd;

import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.core.impl.CaptureProcessor;
import androidx.camera.core.impl.ImageProxyBundle;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {

    //Permission code voor toestemming camera & opslag
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button mMaakFotoBtn;
    ImageView mImageView;
    Uri afbeelding_uri;
    Preview previewView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Assign uit de layout
        mMaakFotoBtn = findViewById(R.id.camera_maakFoto_btn);

        //request a CameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        //check for CameraProvider availability
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(this,cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));



    }

    //Select a camera and bind the life cycle and use cases
    void bindPreview(Context context,@NonNull ProcessCameraProvider cameraProvider) {
        PreviewView previewView = findViewById(R.id.preview_view);

        Size size = new Size (previewView.getWidth(),previewView.getHeight());

        Preview preview = new Preview.Builder()
                .setTargetResolution(size)
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();


        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        ImageCapture imageCapture =
                new ImageCapture.Builder()
                        .build();


        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview,imageCapture);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(getFilesDir()+ "/" + System.currentTimeMillis() + ".jpeg")).build();

        mMaakFotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                                String msg = "Pic captured at " + getFilesDir().toString();
                                Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void onError(ImageCaptureException error) {
                                String msg = "Something went wrong";
                                Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });


        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context),new QrCodeAnlyzer());
        
        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }
}
