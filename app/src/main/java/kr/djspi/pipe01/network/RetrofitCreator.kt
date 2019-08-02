package kr.djspi.pipe01.network

import com.google.gson.GsonBuilder
import kr.djspi.pipe01.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCreator {

    fun createRetrofit(baseUrl: String): RetrofitService {

        fun createOkHttpClient(): OkHttpClient {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            if (BuildConfig.BUILD_TYPE == "debug") {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                val interceptor: HttpLoggingInterceptor =
                    httpLoggingInterceptor.apply {
                        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                    }
                builder.addInterceptor(interceptor)
            }
            return builder.build()
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build().create(RetrofitService::class.java)
    }
}
