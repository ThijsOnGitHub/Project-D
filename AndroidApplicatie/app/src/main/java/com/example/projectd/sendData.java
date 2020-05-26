package com.example.projectd;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class sendData extends AppCompatActivity {

    ArrayList<ImageData> takenImagesArray;
    TextView dataView;

    private RetrofitConnetctions retrofitConnetctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        //connect view to variables
        dataView = findViewById(R.id.mTVDatePreview);

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
        dataView.setText(json+" Sending Data");

        //Get the uri's of the images
        Uri frontImageUri =takenImagesArray.get(0).getImage();
        Uri sideImageUri  = takenImagesArray.get(1).getImage();

        //get the files of the images
        File frontImageFile= new File(getRealPathFromUri(this,frontImageUri));
        File sideImageFile = new File(getRealPathFromUri(this,sideImageUri));

        //Create the request body
        RequestBody requestBodyFront =RequestBody.create(MediaType.parse(getContentResolver().getType(frontImageUri)),frontImageFile);
        RequestBody requestBodySide = RequestBody.create(MediaType.parse(getContentResolver().getType(sideImageUri)),sideImageFile);

        //create the multiparts of the images
        MultipartBody.Part frontImage = MultipartBody.Part.createFormData("frontImage",frontImageFile.getName(),requestBodyFront);
        MultipartBody.Part sideImage = MultipartBody.Part.createFormData("sideImage",sideImageFile.getName(),requestBodySide);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(3000, TimeUnit.SECONDS)
                .connectTimeout(300, TimeUnit.SECONDS)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://localhost:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient);

        Retrofit retrofit = builder.build();

        //Creates all the parameters for the api
        Map<String,Double> parameters =new HashMap<String, Double>();
        MeasurePoints[] measurePoints = MeasurePoints.values();

        for(int i = 0; i<takenImagesArray.size();i++){
            ImageData imageData= takenImagesArray.get(i);
            parameters.put("scale"+firstLetterToUppercase(imageData.getName()),imageData.getRatio());
            Map<MeasurePoints, Double> measurePointsMap = imageData.getMeasurePoints();
            for(int j=0;j<measurePoints.length;j++){
                String name="yLijn"+firstLetterToUppercase(measurePoints[j].toString())+firstLetterToUppercase(imageData.getName());
                parameters.put(name,measurePointsMap.get(measurePoints[j]));
            }
        }

        retrofitConnetctions = retrofit.create(RetrofitConnetctions.class);
        Call<MeasureResult>  call = retrofitConnetctions.measureResult(parameters,frontImage,sideImage);
        call.enqueue(new Callback<MeasureResult>() {
            @Override
            public void onResponse(Call<MeasureResult> call, Response<MeasureResult> response) {
                dataView.setText(response.toString());
            }

            @Override
            public void onFailure(Call<MeasureResult> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }

    public String firstLetterToUppercase(String string){
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    //from https://stackoverflow.com/questions/20028319/how-to-convert-content-media-external-images-media-y-to-file-storage-sdc
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
