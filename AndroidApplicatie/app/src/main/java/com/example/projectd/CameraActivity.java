package com.example.projectd;

import android.Manifest;
import android.content.ContentResolver;
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
import android.view.OrientationEventListener;
import android.view.Surface;
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

import java.io.File;
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
    //What camera chosen
    private int chosencamera;


    private TextToSpeech tts;

    //Elements
    Button mMaakFotoBtn;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    TextView cameraFeedback;
    TextView mQRdetectedText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title & notification bar before setting the content view
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_camera);

        //Connect variables to views
        mMaakFotoBtn = findViewById(R.id.camera_maakFoto_btn);
        mQRdetectedText = findViewById(R.id.QRdetected);
        cameraFeedback = findViewById(R.id.mTVcameraFeedback);


        //Create list with positions to be photographed
        poses = new String[]{"front", "side"};

        //Update the feedback
        updateFeedback();

        //Text to Speech initialization
        tts = new TextToSpeech(getApplicationContext(), status -> {});
        tts.setLanguage(new Locale("nl","NL"));

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
            ContentResolver contentResolver = this.getContentResolver();
            contentResolver.delete(takenImagesArray.get(0).getImage(),null,null);
        }

    }

    void startCamera(){


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
    void updateFeedback(){
        //Set text of the feeback, so that the user knows what to take a picture of
        cameraFeedback.setText("Neem een foto van je "+getText(getResources().getIdentifier(poses[poseIndex],"string",getPackageName())));
    }

    /*
     * Hier wordt de variable qrCodeDetected bijgewerkt en de visablity van de fotoknop bijgewerkt
     * Deze functie wordt aangeroepen tijdens het analyseren
     */
    public void changeBarcodeDetection(List<FirebaseVisionBarcode> barcodes) {
        boolean status = barcodes.size() != 0;
        mQRdetectedText.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
        qrCodeDetected = status;
    }

    ;

    //Vanuit de analyse kan de schaal worden aangepast
    public void tookCorrectImage(double ratio, Uri ImageUri) {
        this.ratio = ratio;
        ImageData data =new ImageData(poses[poseIndex],ImageUri,ratio);
        tts.speak("Foto", TextToSpeech.QUEUE_ADD,null,"1");
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
        } else {
            tts.speak("Neem nu een foto van uw zijkant", TextToSpeech.QUEUE_ADD,null,"1");
            updateFeedback();
            tts.speak("Neem nu een foto van uw zijkant", TextToSpeech.QUEUE_ADD,null,"1");
        }

    }

    public void tookWrongImage(){
       tts.speak("Probeer opnieuw", TextToSpeech.QUEUE_ADD,null,"1");
    }


    //Select a camera and bind the life cycle and use cases
    void bindPreview(Context context, @NonNull ProcessCameraProvider cameraProvider) {
        PreviewView previewView = findViewById(R.id.preview_view);

        Preview preview = new Preview.Builder()
                .build();


        //Camera choice
        if(SettingsActivity.frontcamerachosen){
            chosencamera = CameraSelector.LENS_FACING_FRONT;
        }
        else{
            chosencamera = CameraSelector.LENS_FACING_BACK;
        }

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(chosencamera)
                .build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        ImageCapture imageCapture =
                new ImageCapture.Builder()
                        .build();

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;
                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }
                imageCapture.setTargetRotation(rotation);
                imageAnalysis.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();


        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview, imageCapture);

        //De filename van de foto wordt hier ingesteld




        mMaakFotoBtn.setOnClickListener(v -> new CountDownTimer(timercount, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String msg = String.valueOf(millisUntilFinished / 1000 + 1);
                tts.speak(msg, TextToSpeech.QUEUE_ADD,null,"1");
                mMaakFotoBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinish() {
                mMaakFotoBtn.setVisibility(View.VISIBLE);
                if (mQRdetectedText.getVisibility() == View.VISIBLE){
                    String photoname = System.currentTimeMillis() + ".jpeg";
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, photoname);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                            getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues).build();
                    imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context),
                            new ImageCapture.OnImageSavedCallback() {
                                @Override
                                public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                                    qrCodeAnlyzer.ScanQRcodeFile(context, outputFileResults.getSavedUri());
                                }
                                @Override
                                public void onError(ImageCaptureException error) {
                                    tts.speak("Probeer opnieuw", TextToSpeech.QUEUE_ADD,null,"1");
                                }
                            });
                }
                else{
                    tts.speak("QR Code niet gedetecteerd", TextToSpeech.QUEUE_ADD,null,"1");
                }
            }
        }.start());

        //De imageanalyse wordt hier gestart
        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), qrCodeAnlyzer);


        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

    }
}

