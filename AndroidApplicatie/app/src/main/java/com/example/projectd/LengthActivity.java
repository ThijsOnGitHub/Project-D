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

        Intent settings = new Intent(LengthActivity.this, SettingsActivity.class);
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
