package com.example.kkarhua.data.remote

import com.google.gson.annotations.SerializedName
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

    @SerializedName("image")
    val image: XanoImage?,

    val category: String,
    val stock: Int
)

data class XanoImage(
    val path: String?,
    val name: String?,
    val type: String?,
    val size: Int?,
    val mime: String?,
    val meta: ImageMeta?,
    val url: String?
)

data class ImageMeta(
    val width: Int?,
    val height: Int?
)

fun XanoImage?.getImageUrl(): String {
    return this?.url ?: this?.path ?: ""
}