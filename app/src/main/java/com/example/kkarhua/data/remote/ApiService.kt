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
        @Part image: MultipartBody.Part
    ): Response<ProductResponse>

    // ✅ NUEVO: Endpoint para actualizar producto
    @Multipart
    @PATCH("product/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part image: MultipartBody.Part?  // Opcional - solo si se cambia la imagen
    ): Response<ProductResponse>

    // ✅ NUEVO: Endpoint para eliminar producto
    @DELETE("product/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>
}

// Modelo principal de respuesta de producto
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
    val image: XanoImage?
)

// Estructura para manejar el storage de Xano
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

// Extensión para obtener la URL de forma segura
fun XanoImage?.getImageUrl(): String {
    return this?.url ?: this?.path ?: ""
}