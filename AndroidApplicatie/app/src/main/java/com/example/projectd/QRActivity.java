package com.example.projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

public class QRActivity extends AppCompatActivity {
    private static final int CREATE_FILE = 1;
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
            PrintHelper photoPrinter = new PrintHelper(this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FILL);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.qrcode10cm);
            photoPrinter.printBitmap("QR code 10 cm", bitmap);

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
