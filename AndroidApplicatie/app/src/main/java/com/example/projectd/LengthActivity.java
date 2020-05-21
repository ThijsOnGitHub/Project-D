package com.example.projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LengthActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1000;
    Button BackButton;
    Button ForwardButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        this.setContentView(R.layout.activity_length);

        //TODO gather inputted length data

        Intent camera = new Intent(LengthActivity.this, CameraActivity.class);
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
