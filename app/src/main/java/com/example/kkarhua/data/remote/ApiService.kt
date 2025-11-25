package com.example.kkarhua.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("product")
    suspend fun getAllProducts(): Response<List<ProductResponse>>

    @GET("product/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<ProductResponse>

    @Multipart
    @POST("product")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("category") category: RequestBody,
        @Part image: MultipartBody.Part,
        @Part image2: MultipartBody.Part?, // ✅ Permitir nulo para la subida de una sola imagen
        @Part image3: MultipartBody.Part?  // ✅ Permitir nulo para la subida de una sola imagen
    ): Response<ProductResponse>

    @PATCH("product/{id}")
    @Headers("Content-Type: application/json")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body productData: UpdateProductData
    ): Response<ProductResponse>

    @DELETE("product/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>
}

// Data class para actualizar productos (rest of the file remains unchanged)
data class UpdateProductData(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val image: ImageUpdateData? = null,
    val image2: ImageUpdateData? = null, // ✅ NUEVO
    val image3: ImageUpdateData? = null  // ✅ NUEVO
)
// ... (rest of the file remains unchanged)

data class ImageUpdateData(
    val path: String,
    val name: String,
    val type: String,
    val size: Int,
    val mime: String,
    val url: String,
    val meta: ImageMetaUpdate
)

data class ImageMetaUpdate(
    val width: Int,
    val height: Int
)

data class ProductResponse(
    val id: Int,

    @SerializedName("created_at")
    val createdAt: Long?,

    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,

    @SerializedName("updated_at")
    val updatedAt: Long?,

    @SerializedName("is_deleted")
    val isDeleted: Boolean?,

    @SerializedName("image")
    val image: XanoImage?,

    @SerializedName("image2") // ✅ NUEVO
    val image2: XanoImage?,

    @SerializedName("image3") // ✅ NUEVO
    val image3: XanoImage?
)

data class XanoImage(
    val access: String?,
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