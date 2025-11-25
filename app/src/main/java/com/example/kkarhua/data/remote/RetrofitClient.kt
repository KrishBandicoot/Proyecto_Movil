package com.example.kkarhua.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ✅ ENDPOINTS SEPARADOS
    private const val BASE_URL_PRODUCTS = "https://x8ki-letl-twmt.n7.xano.io/api:2bSFBtEo/"
    private const val BASE_URL_AUTH = "https://x8ki-letl-twmt.n7.xano.io/api:YeqJmQI7/"
    private const val BASE_URL_USERS = "https://x8ki-letl-twmt.n7.xano.io/api:abImCnIy/"
    private const val BASE_URL_ADDRESS = "https://x8ki-letl-twmt.n7.xano.io/api:I6D05L_b/"
    private const val BASE_URL_PURCHASE = "https://x8ki-letl-twmt.n7.xano.io/api:z-bs0IRt/"
    private const val BASE_URL_PURCHASE_ITEM = "https://x8ki-letl-twmt.n7.xano.io/api:G3c9N9A7/"

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

    private val retrofitUsers: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USERS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ NUEVO: Retrofit para Address
    private val retrofitAddress: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_ADDRESS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ NUEVO: Retrofit para Purchase
    private val retrofitPurchase: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PURCHASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ NUEVO: Retrofit para Purchase Item
    private val retrofitPurchaseItem: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PURCHASE_ITEM)
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

    val userService: UserService by lazy {
        retrofitUsers.create(UserService::class.java)
    }

    // ✅ NUEVO: Servicios de compras
    val addressService: PurchaseService by lazy {
        retrofitAddress.create(PurchaseService::class.java)
    }

    val purchaseService: PurchaseService by lazy {
        retrofitPurchase.create(PurchaseService::class.java)
    }

    val purchaseItemService: PurchaseService by lazy {
        retrofitPurchaseItem.create(PurchaseService::class.java)
    }
}