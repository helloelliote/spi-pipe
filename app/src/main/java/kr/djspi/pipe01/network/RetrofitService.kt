package kr.djspi.pipe01.network

import com.google.gson.JsonObject
import kr.djspi.pipe01.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

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
    fun getServerData(
        @Query("json") jsonString: String
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @GET("api")
    fun getSpi(
        @Query("json") jsonString: String
    ): Call<JsonObject>
}