package com.example.projectd;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class sendData extends AppCompatActivity {

    ArrayList<ImageData> takenImagesArray;
    TextView header;
    TextView dataView;
    TextView footer;

    private RetrofitConnetctions retrofitConnetctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        this.getSupportActionBar().hide();

        //connect view to variables
        header = findViewById(R.id.header);
        dataView = findViewById(R.id.mTVDatePreview);
        footer = findViewById(R.id.footer);

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
        dataView.setText(json);

        //Contentresolver to delete pictures after use
        ContentResolver contentResolver = this.getContentResolver();

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
                .readTimeout(5000, TimeUnit.SECONDS)
                .writeTimeout(5000,TimeUnit.MINUTES)
                .connectTimeout(5000, TimeUnit.SECONDS)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://projectd.martijnnieuwenhuis.nl/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient);

        Retrofit retrofit = builder.build();

        //Creates all the parameters for the api
        Map<String,Double> scales = new HashMap<>();
        Map<String,Integer> yLijnen =new HashMap<String, Integer>();
        MeasurePoints[] measurePoints = MeasurePoints.values();

        for (ImageData imageData : takenImagesArray) {
            scales.put("scale" + firstLetterToUppercase(imageData.getName()), imageData.getRatio());
            Map<MeasurePoints, Integer> measurePointsMap = imageData.getMeasurePoints();
            for (MeasurePoints measurePoint : measurePoints) {
                String name = "yLine" + firstLetterToUppercase(measurePoint.toString()) + firstLetterToUppercase(imageData.getName());
                yLijnen.put(name, measurePointsMap.get(measurePoint));
            }
        }

        retrofitConnetctions = retrofit.create(RetrofitConnetctions.class);



        //Data sent back
        Call<HashMap<String, Double>> call = retrofitConnetctions.measureResult(scales,yLijnen,frontImage,sideImage);

        call.enqueue(new Callback<HashMap<String, Double>>() {
            @Override
            public void onResponse(Call<HashMap<String, Double>> call, Response<HashMap<String, Double>> response) {
                if(response.isSuccessful()){
                    Intent toResult = new Intent(getApplicationContext(),ShowMessureData.class);
                    toResult.putExtra("results", response.body());
                    startActivity(toResult);

                    //Deletes used pictures from phone
                    contentResolver.delete(frontImageUri,null,null);
                    contentResolver.delete(sideImageUri,null,null);
                }else{
                    Log.i("result",response.toString());
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, Double>> call, Throwable throwable) {
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
