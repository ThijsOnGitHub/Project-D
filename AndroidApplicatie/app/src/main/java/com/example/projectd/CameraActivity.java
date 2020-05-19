package com.example.projectd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Size;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    //Permission code voor toestemming camera & opslag

    //Qr code is aanwezig in de afbeelding
    private  boolean qrCodeDetected = false;
    //Qr code schaal
    private double verhoudingsGetal;

    public QrCodeAnlyzer qrCodeAnlyzer= new QrCodeAnlyzer(this);
    Button mMaakFotoBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title & notification bar before setting the content view
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_camera);

        mMaakFotoBtn= findViewById(R.id.camera_maakFoto_btn);


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
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /*
    * Hier wordt de variable qrCodeDetected bijgewerkt en de visablity van de fotoknop bijgewerkt
    * Deze functie wordt aangeroepen tijdens het analyseren
     */
    public void changeBarcodeDetection( List<FirebaseVisionBarcode> barcodes){
        boolean status=barcodes.size()!=0;
        mMaakFotoBtn.setVisibility(status?View.VISIBLE:View.INVISIBLE);
        qrCodeDetected=status;
    };

    //Vanuit de analyse kan de schaal worden aangepast
    public void setVerhoudingsGetal(double verhoudingsGetal){
        this.verhoudingsGetal = verhoudingsGetal;
        mMaakFotoBtn.setText(Double.toString(verhoudingsGetal));
    }


    //Select a camera and bind the life cycle and use cases
    void bindPreview(Context context,@NonNull ProcessCameraProvider cameraProvider) {
        PreviewView previewView = findViewById(R.id.preview_view);


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


        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis,  preview,imageCapture);

        //De filename van de foto wordt hier ingesteld


/*

                */

        mMaakFotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis());
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues).build();

                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                                String msg = "Pic captured at " + getFilesDir().toString();
                                Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                                qrCodeAnlyzer.ScanQRcodeFile(context,outputFileResults.getSavedUri());
                            }
                            @Override
                            public void onError(ImageCaptureException error) {
                                String msg = "Something went wrong";
                                Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        //De imageanalyse wordt hier gestart
        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), qrCodeAnlyzer);

        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }


}
