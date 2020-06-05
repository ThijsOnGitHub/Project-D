package com.example.projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Size;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    //Qr code is aanwezig in de afbeelding
    private  boolean qrCodeDetected = false;
    //Qr code schaal
    private double ratio;

    //Take image varible
    private String[] poses;
    private int poseIndex=0;
    private ArrayList<ImageData> takenImagesArray;

    //Data
    public QrCodeAnlyzer qrCodeAnlyzer= new QrCodeAnlyzer(this);



    //Elements
    Button mMaakFotoBtn;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    TextView cameraFeedback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title & notification bar before setting the content view
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_camera);

        //Connect variables to views
        mMaakFotoBtn= findViewById(R.id.camera_maakFoto_btn);
        cameraFeedback = findViewById(R.id.mTVcameraFeedback);

        //Create list with positions to be photographed
        poses = new String[]{"front","side"};

        //Update the feeback
        updateFeedback();

        //Intiate takenImageMap
        takenImagesArray = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        poseIndex=poses.length-1;
    }

    @Override
    public void onBackPressed() {
        if(poseIndex==0){
            super.onBackPressed();
        }else{
            poseIndex--;
            updateFeedback();
        }

    }

    void startCamera(){


        //request a CameraProvider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        //check for CameraProvider availability
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(this,cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

    }






    void updateFeedback(){
        //Set text of the feeback, so that the user knows what to take a picture of
        cameraFeedback.setText("Neem een foto van je "+getText(getResources().getIdentifier(poses[poseIndex],"string",getPackageName())));
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
    public void tookCorrectImage(double ratio,Uri ImageUri){
        this.ratio = ratio;
        ImageData data =new ImageData(poses[poseIndex],ImageUri,ratio);

        //overrides if it don't exists
        if(poseIndex<takenImagesArray.size()){
            takenImagesArray.set(poseIndex,data);
        }else{
            takenImagesArray.add(data);
        }
        poseIndex++;
        //checks if all the poses are done
        if(poseIndex>poses.length-1){
            Intent nextIntent= new Intent(this,SetLines.class);
            nextIntent.putExtra("data", takenImagesArray);
            cameraProvider.unbindAll();
            this.startActivity(nextIntent);
        }else{
            updateFeedback();
        }

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
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis()+".jpeg");
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
