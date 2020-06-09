package com.example.projectd;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.HashMap;
import java.util.Map;

public interface RetrofitConnetctions {

    @Multipart
    @POST("measure")
    Call<HashMap<String,Double>> measureResult(
            @PartMap Map<String, Double> scale,
            @PartMap Map<String, Integer> yLijnen,
            @Part MultipartBody.Part frontImage,
            @Part MultipartBody.Part sideImage
    );
}
