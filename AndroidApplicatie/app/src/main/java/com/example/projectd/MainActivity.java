package com.example.projectd;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.*;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1000;
    Button ToQRCodeButton;
    Button ToLengthButton;
    Button HelpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        this.setContentView(R.layout.activity_main);

        //The Buttons
        ToQRCodeButton = findViewById(R.id.ToQRButton);
        ToLengthButton = findViewById(R.id.ToLength);
        HelpButton = findViewById(R.id.Help);

        //The other pages
        Intent camera = new Intent(MainActivity.this, CameraActivity.class);
        Intent qr = new Intent(MainActivity.this, QRActivity.class);
        Intent length = new Intent(MainActivity.this, LengthActivity.class);
        Intent help = new Intent(MainActivity.this, HelpActivity.class);

        //Check & Ask Permission
        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permission, PERMISSION_CODE);
        }

        //OnClicks
        ToQRCodeButton.setOnClickListener(v -> {

            //Move this to the actual camera place
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_CODE);
            }
            else{
                startActivity(camera);
            }
        });
        ToLengthButton.setOnClickListener(v ->{
            startActivity(length);
        });
        HelpButton.setOnClickListener(v ->{
            startActivity(help);
        });
    }
}