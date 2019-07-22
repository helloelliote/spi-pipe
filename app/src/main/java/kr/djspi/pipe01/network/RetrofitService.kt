package kr.djspi.pipe01.network

import com.google.gson.JsonObject
import kr.djspi.pipe01.BuildConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @Headers(
        "X-NCP-APIGW-API-KEY-ID: ${BuildConfig.CLIENT_ID}",
        "X-NCP-APIGW-API-KEY: ${BuildConfig.CLIENT_SECRET}"
    )
    @GET("search")
    fun searchPlaces(
        @Query("query") query: String,
        @Query("coordinate") coordinate: String
    ): Call<JsonObject>

    @Headers(
        "X-NCP-APIGW-API-KEY-ID: ${BuildConfig.CLIENT_ID}",
        "X-NCP-APIGW-API-KEY: ${BuildConfig.CLIENT_SECRET}"
    )
    @GET("gc")
    fun reverseGeocode(
        @Query("coords") coords: String,
        @Query("orders") orders: String = "roadaddr",
        @Query("output") output: String = "json"
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @GET("api")
    fun getSuperviseDatabase(
        @Query("json") jsonString: String
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @GET("api")
    fun getSpi(
        @Query("json") jsonString: String
    ): Call<JsonObject>

    @Multipart
    @POST("api")
    fun postSpi(
        @Part("json") jsonRequestBody: RequestBody,
        @Part fileMultipartBody: MultipartBody.Part?
    ): Call<JsonObject>
}