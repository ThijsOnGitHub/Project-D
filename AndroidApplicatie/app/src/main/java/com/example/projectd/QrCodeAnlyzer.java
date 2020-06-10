package com.example.projectd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.IOException;
import java.util.List;


public class QrCodeAnlyzer implements ImageAnalysis.Analyzer {

    private  CameraActivity cameraActivity;

    QrCodeAnlyzer( CameraActivity cameraActivity){
        super();
        this.cameraActivity =cameraActivity;
   }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    //Na foto maken owrdt
    public void ScanQRcodeFile(Context context,Uri imageFile){
        Log.i( "self","inf Scan started on image:"+imageFile.toString());
        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(context, imageFile);
            setRatio(image,imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRatio(FirebaseVisionImage image, Uri imageUri){
        getBarcodesTask(image)
            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                // Task completed successfully
                // ...
                Log.i("barcodes",barcodes.toString());
                for (FirebaseVisionBarcode barcode: barcodes) {
                    //Alleen als de waarde van de qr-code start met
                    String rawValue =barcode.getRawValue();
                    Log.i("Qr code data",rawValue);
                    Log.i("Match Data",rawValue.matches("^TashiraApp.*")?"true":"false");
                    if(rawValue.matches("^TashiraApp.*")){

                        Point[] corners = barcode.getCornerPoints();
                        for (int i = 0; i < corners.length; i++) {
                            Log.i("inf: corners", corners[i].toString());
                        }

                        Point point1 = corners[0];
                        Point point2 = corners[1];

                        //Berekend het aantal centimeters tussen de punten
                        double pixelsWide = Math.sqrt(Math.pow(point2.x - point1.x, 2) - Math.pow(point2.y - point1.y, 2));
                        Log.i("aantal pixels", Double.toString(pixelsWide));

                        //krijgt het aantal milimeters uit de qr-code
                        String millimeters=rawValue.replace("TashiraApp ","");
                        double mellimetersWide=Double.parseDouble(millimeters);

                        Log.i("aantal milimeters",mellimetersWide+"");
                        //Geeft een verhoudingsgetal
                        double ratio= mellimetersWide/pixelsWide;
                        Log.i("ratio",ratio+"");
                        cameraActivity.tookCorrectImage(ratio,imageUri);
                        return;
                    }
                }
                cameraActivity.tookWrongImage();
            }
            })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }
    //Geeft een task van de gedetecteerde barcodes
    private Task<List<FirebaseVisionBarcode>> getBarcodesTask(FirebaseVisionImage image){
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image);

        return result;
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        int degrees = imageProxy.getImageInfo().getRotationDegrees();

        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();

        int rotation = degreesToFirebaseRotation(degrees);
        FirebaseVisionImage image =
                FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
        // Pass image to an ML Kit Vision API

        getBarcodesTask(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                // Task completed successfully
                // ...
                cameraActivity.changeBarcodeDetection(barcodes);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Task failed with an exception
                // ...
                Log.i("inf:","Zo te zien is dit een failure");
                e.getLocalizedMessage();
            }
        });

        //Als deze functie wordt aangeroepen wordt de afbeelding opnieuw gescand
        imageProxy.close();
    }
}
