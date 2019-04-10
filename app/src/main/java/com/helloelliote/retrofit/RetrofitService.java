package com.helloelliote.retrofit;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import static kr.djspi.pipe01.BuildConfig.NAVER_CLIENT_ID;
import static kr.djspi.pipe01.BuildConfig.NAVER_CLIENT_SECRET;

interface RetrofitService {

    String NAVER_KEY_CLIENT_ID = "X-NCP-APIGW-API-KEY-ID";
    String NAVER_KEY_CLIENT_SECRET = "X-NCP-APIGW-API-KEY";

    @Headers({
            NAVER_KEY_CLIENT_ID + ": " + NAVER_CLIENT_ID,
            NAVER_KEY_CLIENT_SECRET + ": " + NAVER_CLIENT_SECRET})
    @GET("search")
    Call<JsonObject> getSearchPlaces(
            @Query("query") String query,
            @Query("coordinate") String coordinate
    );

    @Headers("Content-Type: application/json")
    @GET("api")
    Call<JsonObject> getSpi(
            @Query("json") String jsonString
    );

    @Headers("Content-Type: application/json")
    @GET("api")
    Call<JsonObject> getSupervise(
            @Query("json") String jsonString
    );

    @POST("api")
    Call<JsonObject> postSpi(
            @Query("json") String jsonString
    );

    @POST("api")
    @Multipart
    Call<JsonObject> postSpiMultipart(
            @Part("json") RequestBody jsonString,
            @Part MultipartBody.Part file
    );
}
