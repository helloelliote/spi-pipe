package kr.djspi.pipe01.network

import com.google.gson.GsonBuilder
import kr.djspi.pipe01.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCreator {

    fun createRetrofit(baseUrl: String): RetrofitService {

        fun createOkHttpClient(): OkHttpClient {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = if (BuildConfig.DEBUG) BODY else NONE
            builder.addInterceptor(interceptor)
            return builder.build()
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build().create(RetrofitService::class.java)
    }
}
