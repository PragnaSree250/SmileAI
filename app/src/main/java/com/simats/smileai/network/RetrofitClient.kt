package com.simats.smileai.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Updated to your IP: 180.235.121.253
    const val BASE_URL = "http://180.235.121.253:8116/"
    var authToken: String? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            
            val currentToken = authToken
            if (!currentToken.isNullOrBlank()) {
                // Ensure Bearer prefix is used and formatted correctly
                val tokenToUse = if (currentToken.startsWith("Bearer ", ignoreCase = true)) {
                    currentToken
                } else {
                    "Bearer $currentToken"
                }
                requestBuilder.addHeader("Authorization", tokenToUse)
            }
            
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        retrofit.create(ApiService::class.java)
    }
}
