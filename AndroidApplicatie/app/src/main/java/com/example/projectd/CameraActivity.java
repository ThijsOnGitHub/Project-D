package com.example.projectd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {

    //Permission code voor toestemming camera & opslag
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button mMaakFotoBtn;
    ImageView mImageView;
    Uri afbeelding_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Assign uit de layout
        mImageView = findViewById(R.id.image_view);
        mMaakFotoBtn = findViewById(R.id.camera_maakFoto_btn);

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

    private void openCamera() {
        //Camera intent & gemaakte afbeelding opslaan
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "Project-D");
        afbeelding_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, afbeelding_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
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
        if(resultCode == RESULT_OK){
            mImageView.setImageURI(afbeelding_uri);
        }
    }
}
