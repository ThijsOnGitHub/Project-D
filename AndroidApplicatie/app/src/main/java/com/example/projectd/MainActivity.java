package com.example.projectd;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1000;
    Button ToQRCodeButton;
    Button HelpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        this.setContentView(R.layout.activity_main);

        //The Buttons
        ToQRCodeButton = findViewById(R.id.ToQRButton);
        HelpButton = findViewById(R.id.Help);

        //The other pages
        Intent qr = new Intent(MainActivity.this, QRActivity.class);
        Intent length = new Intent(MainActivity.this, LengthActivity.class);
        Intent help = new Intent(MainActivity.this, HelpActivity.class);

        //Check & Ask Permission
        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.INTERNET) ==PackageManager.PERMISSION_DENIED
        ){
            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET};
            requestPermissions(permission, PERMISSION_CODE);
        }

        //OnClicks
        ToQRCodeButton.setOnClickListener(v ->{
            startActivity(qr);
        });
        HelpButton.setOnClickListener(v ->{
            startActivity(help);
        });
    }
}
