package com.example.projectd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
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
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        mImageView = findViewById(R.id.image_view);
        mMaakFotoBtn = findViewById(R.id.camera_maakFoto_btn);

        //Ask permission
        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

            //Om toestemming vragen.
            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permission, PERMISSION_CODE);
        }
        //request a CameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        ImageCapture imageCapture =
                new ImageCapture.Builder().build();

        //check for CameraProvider availability
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));


        //Op maakFotoKnop gedrukt
        mMaakFotoBtn.setOnClickListener(v -> {
            //Indien geen toestemming voor camera en opslag
            takePhoto(imageCapture);
        });

    }

    //Select a camera and bind the life cycle and use cases
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider, ImageCapture imageCapture) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);

        PreviewView previewView = findViewById(R.id.preview_view);
        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }

    public void takePhoto(ImageCapture imageCapture) {
        ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(new File(getFilesDir()+ "/" + System.currentTimeMillis() + ".raw")).build();


        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Looper.prepare();
                        String msg = "Pic captured at " + getFilesDir().toString();
                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        Looper.prepare();
                        String msg = "Something went wrong";
                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                    }
        });
    }

    //Resultaat van gevraagde toestemming afhandelen.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //TakePhoto();
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
