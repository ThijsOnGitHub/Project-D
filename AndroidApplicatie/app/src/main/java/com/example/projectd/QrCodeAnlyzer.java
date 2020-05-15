package com.example.projectd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

public class QrCodeAnlyzer implements ImageAnalysis.Analyzer {

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

    public void ScanQRcodeFile(Context context,File imageFile){
        Log.i( "self","inf Scan started");
        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(context, Uri.parse("file://"+imageFile.getAbsolutePath()));
            giveResults(image);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void giveResults(FirebaseVisionImage image){
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        // ...
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            Point[] corners = barcode.getCornerPoints();
                            for (int i = 0; i < corners.length; i++) {
                                Log.i("inf: corners", corners[i].toString());
                            }
                            Point point1 = corners[0];
                            Point point2 = corners[1];

                            double length = Math.sqrt(Math.pow(point2.x - point1.x, 2) - Math.pow(point2.y - point1.y, 2));
                            Log.i("inf: length", Double.toString(length));

                            String rawValue = barcode.getRawValue();
                            Log.i("inf: data",rawValue);

                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                    String ssid = barcode.getWifi().getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    break;
                                case FirebaseVisionBarcode.TYPE_TEXT:
                                    Log.i("inf text", barcode.getRawValue());
                                    break;
                            }

                        }
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
        Log.i("inf:","test1234");
        giveResults(image);
        imageProxy.close();
    }
}
