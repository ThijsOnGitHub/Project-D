package com.example.projectd;

import android.os.Looper;
import android.util.Size;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
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


        //Op maakFotoKnop gedrukt
        mMaakFotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Indien geen toestemming voor camera en opslag
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

                    //Om toestemming vragen.
                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, PERMISSION_CODE);
                }else{
                    //Heeft toestemming
                    openCamera();
                }
            }
        });

    }

    //Select a camera and bind the life cycle and use cases
    void bindPreview(Context context,@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
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
        

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview,imageAnalysis,imageCapture);

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(new File(getFilesDir()+ "/" + System.currentTimeMillis() + ".jpeg")).build();

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

        PreviewView previewView = findViewById(R.id.preview_view);
        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }

    //Deze functie voert de anlyses op de camera uit
    private void openCamera() {

    }

    //Resultaat van gevraagde toestemming afhandelen.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }else{
                    Toast.makeText(this, "Benodigde toestemming geweigerd.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Afbeelding met camera gemaakt, toon in imageView.
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mImageView.setImageURI(afbeelding_uri);
        }
    }
}
