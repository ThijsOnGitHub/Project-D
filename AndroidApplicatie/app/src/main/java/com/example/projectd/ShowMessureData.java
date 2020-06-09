package com.example.projectd;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashMap;

public class ShowMessureData extends AppCompatActivity {

    //The measured data from the previous activity
    private HashMap<String,Double> measureResults;
    private MeasureResults resultsObject;
    //Set all the views
    private TextView resultTextView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messure_data);

        //Set views
        resultTextView = findViewById(R.id.mTVresultaat);
        backButton = findViewById(R.id.mBTrestartApp);

        //Gets the data from the previous activity
        measureResults = (HashMap<String, Double>) getIntent().getSerializableExtra("results");

        resultsObject = new MeasureResults(measureResults);
        //Sets the results object
        String resultText=resultsObject.getResultString(getApplicationContext(),MeasureResultsUnit.centimeter,2);
        //Put the data into a text

        resultTextView.setText(resultText);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartApp();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    void restartApp(){
        Intent StartOver = new Intent(getApplicationContext(),MainActivity.class);
        StartOver.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        StartOver.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        StartOver.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(StartOver);
    }
}
