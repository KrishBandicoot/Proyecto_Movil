package com.example.kkarhua.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("product")
    suspend fun getAllProducts(): Response<List<ProductResponse>>

    @GET("product/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<ProductResponse>
}

data class ProductResponse(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int
)