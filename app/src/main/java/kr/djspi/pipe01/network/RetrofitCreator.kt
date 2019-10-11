package kr.djspi.pipe01.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCreator {

    fun createRetrofit(baseUrl: String): RetrofitService {

        fun createOkHttpClient(): OkHttpClient {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            return builder.build()
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build().create(RetrofitService::class.java)
    }
}
