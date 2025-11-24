package com.example.kkarhua.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL_PRODUCTS = "https://x8ki-letl-twmt.n7.xano.io/api:2bSFBtEo/"
    private const val BASE_URL_AUTH = "https://x8ki-letl-twmt.n7.xano.io/api:YeqJmQI7/"

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofitProducts: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PRODUCTS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitAuth: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AUTH)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofitProducts.create(ApiService::class.java)
    }

    val authService: AuthService by lazy {
        retrofitAuth.create(AuthService::class.java)
    }

    // ✅ CORREGIDO: UserService debe estar en la MISMA API donde están los endpoints
    // Según tus capturas de Xano, prueba primero con retrofitAuth
    val userService: UserService by lazy {
        retrofitAuth.create(UserService::class.java)
    }
}