package com.example.projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class CameraActivity extends AppCompatActivity {
    //Qr code is aanwezig in de afbeelding
    private boolean qrCodeDetected = false;
    //Qr code schaal
    private double ratio;

    //Take image varible
    private String[] poses;
    private int poseIndex = 0;
    private ArrayList<ImageData> takenImagesArray;

    //Data
    public QrCodeAnlyzer qrCodeAnlyzer = new QrCodeAnlyzer(this);

    //Timer stuff
    long timercount = SettingsActivity.timer * 1000;

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
        mMaakFotoBtn = findViewById(R.id.camera_maakFoto_btn);
        cameraFeedback = findViewById(R.id.mTVcameraFeedback);

        //Create list with positions to be photographed
        poses = new String[]{"front", "side"};

        //Update the feeback
        updateFeedback();

        //Intiate takenImageMap
        takenImagesArray = new ArrayList<>();

        //request a CameraProvider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        //check for CameraProvider availability
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(this, cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void updateFeedback() {
        //Set text of the feeback, so that the user knows what to take a picture of
        cameraFeedback.setText("Neem een foto van je " + getText(getResources().getIdentifier(poses[poseIndex], "string", getPackageName())));
    }

    /*
     * Hier wordt de variable qrCodeDetected bijgewerkt en de visablity van de fotoknop bijgewerkt
     * Deze functie wordt aangeroepen tijdens het analyseren
     */
    public void changeBarcodeDetection(List<FirebaseVisionBarcode> barcodes) {
        boolean status = barcodes.size() != 0;
        mMaakFotoBtn.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
        qrCodeDetected = status;
    }

    ;

    //Vanuit de analyse kan de schaal worden aangepast
    public void tookCorrectImage(double ratio, Uri ImageUri) {
        this.ratio = ratio;
        takenImagesArray.add(new ImageData(poses[poseIndex], ImageUri, ratio));
        poseIndex++;
        if (poseIndex >= poses.length) {
            Intent nextIntent = new Intent(this, SetLines.class);
            nextIntent.putParcelableArrayListExtra("data", takenImagesArray);
            cameraProvider.unbindAll();
            finish();
            this.startActivity(nextIntent);
        } else {
            updateFeedback();
        }

    }


    //Select a camera and bind the life cycle and use cases
    void bindPreview(Context context, @NonNull ProcessCameraProvider cameraProvider) {
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

        //Text to Speech initialization
        TextToSpeech tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        //No Dutch language available atm
        tts.setLanguage(Locale.ENGLISH);

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview, imageCapture);

        //De filename van de foto wordt hier ingesteld

        mMaakFotoBtn.setOnClickListener(v -> new CountDownTimer(timercount, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //TODO insert text to speech
                String msg = String.valueOf(millisUntilFinished / 1000 + 1);
                tts.speak(msg, TextToSpeech.QUEUE_ADD,null,"1");
            }

            @Override
            public void onFinish() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpeg");
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
                                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                                qrCodeAnlyzer.ScanQRcodeFile(context, outputFileResults.getSavedUri());
                                tts.speak("Photo", TextToSpeech.QUEUE_ADD,null,"2");
                            }
                            @Override
                            public void onError(ImageCaptureException error) {
                                String msg = "Something went wrong";
                                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                                tts.speak("Try Again", TextToSpeech.QUEUE_ADD,null,"3");
                            }
                        });
            }
        }.start());

        //De imageanalyse wordt hier gestart
        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), qrCodeAnlyzer);


        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }
}

