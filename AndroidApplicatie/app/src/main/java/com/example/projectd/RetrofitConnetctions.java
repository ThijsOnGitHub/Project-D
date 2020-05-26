package com.example.projectd;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface RetrofitConnetctions {
    @Multipart
    @POST("measure")
    Call<MeasureResult> measureResult(
            @QueryMap Map<String, Double> parameters,
            @Part MultipartBody.Part frontImage,
            @Part MultipartBody.Part sideImage
    );
}
