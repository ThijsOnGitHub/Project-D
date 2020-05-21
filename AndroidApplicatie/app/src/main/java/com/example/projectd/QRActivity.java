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
    ImageButton PrintQRButton;
    Button ForwardButton;
    Button BackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        this.setContentView(R.layout.activity_qr); //it does work but it gives an error here

        Intent camera = new Intent(QRActivity.this, CameraActivity.class);
        PrintQRButton = findViewById(R.id.print);
        PrintQRButton.setOnClickListener(v ->{
            //TODO insert print code here
        });
        ForwardButton = findViewById(R.id.Camera);
        ForwardButton.setOnClickListener(v ->{
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_CODE);
            }
            else{
                startActivity(camera);
            }
        });
        BackButton = findViewById(R.id.Terug);
        BackButton.setOnClickListener(v ->{
            finish();
        });
    }
}
