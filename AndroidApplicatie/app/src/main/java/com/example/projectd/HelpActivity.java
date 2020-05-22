package com.example.projectd;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    //TODO Add helpful information
    //TODO make scrollable if needed
    Button BackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        this.setContentView(R.layout.activity_help);
        BackButton = findViewById(R.id.back);
        BackButton.setOnClickListener(v ->{
           finish();
        });
    }
}
