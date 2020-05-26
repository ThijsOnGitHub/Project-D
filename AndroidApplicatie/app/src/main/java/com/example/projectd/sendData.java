package com.example.projectd;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class sendData extends AppCompatActivity {

    ArrayList<ImageData> takenImagesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        Intent intent = getIntent();
        takenImagesArray = intent.getParcelableArrayListExtra("data");
        String json= new Gson().toJson(takenImagesArray);
        JSONObject jsonObject=null;
        try {
             jsonObject= new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("json",json);
        ((TextView)findViewById(R.id.mTVTest)).setText(json);
    }
}
