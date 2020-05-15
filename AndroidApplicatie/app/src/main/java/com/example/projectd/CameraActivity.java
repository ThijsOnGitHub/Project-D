package com.example.projectd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
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
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

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
        
        //request a CameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        //check for CameraProvider availability
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
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
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);

        PreviewView previewView = findViewById(R.id.preview_view);
        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }


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
