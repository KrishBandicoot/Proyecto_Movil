package com.example.kkarhua.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL_PRODUCTS = "https://x8ki-letl-twmt.n7.xano.io/api:kJUj45sO/"
    private const val BASE_URL_AUTH = "https://x8ki-letl-twmt.n7.xano.io/api:Ilv8KuLd/"

    private val retrofitProducts: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PRODUCTS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitAuth: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofitProducts.create(ApiService::class.java)
    }

    val authService: AuthService by lazy {
        retrofitAuth.create(AuthService::class.java)
    }
}