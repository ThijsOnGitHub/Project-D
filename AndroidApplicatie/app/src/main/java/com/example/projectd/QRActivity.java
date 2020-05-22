package com.example.projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class QRActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1000;
    Button PrintQRButton;
    Button ForwardButton;
    Button BackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        this.setContentView(R.layout.activity_qr);
        PrintQRButton = findViewById(R.id.print);
        PrintQRButton.setOnClickListener(v ->{
            //TODO insert print code here
        });

        Intent settings = new Intent(QRActivity.this, SettingsActivity.class);
        ForwardButton = findViewById(R.id.forward);
        ForwardButton.setOnClickListener(v ->{
            startActivity(settings);
        });
        BackButton = findViewById(R.id.back);
        BackButton.setOnClickListener(v ->{
            finish();
        });
    }
}
