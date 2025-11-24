package com.example.kkarhua.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface UserService {

    @GET("user")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<List<UserResponse>>

    @GET("user/{user_id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("user_id") userId: Int
    ): Response<UserResponse>

    // ✅ SOLUCIÓN: Crear dos métodos separados
    // Método 1: Para actualizar SIN contraseña
    @PATCH("user/{user_id}")
    @Headers("Content-Type: application/json")
    suspend fun updateUserWithoutPassword(
        @Header("Authorization") token: String,
        @Path("user_id") userId: Int,
        @Body userData: UpdateUserDataWithoutPassword
    ): Response<UserResponse>

    // Método 2: Para actualizar CON contraseña
    @PATCH("user/{user_id}")
    @Headers("Content-Type: application/json")
    suspend fun updateUserWithPassword(
        @Header("Authorization") token: String,
        @Path("user_id") userId: Int,
        @Body userData: UpdateUserDataWithPassword
    ): Response<UserResponse>

    @DELETE("user/{user_id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("user_id") userId: Int
    ): Response<DeleteUserResponse>
}

// ✅ Data classes FUERA de la interfaz
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,

    @SerializedName("created_at")
    val created_at: Long?
)

// ✅ Para actualizar SIN contraseña
data class UpdateUserDataWithoutPassword(
    val name: String,
    val email: String,
    val role: String
)

// ✅ Para actualizar CON contraseña
data class UpdateUserDataWithPassword(
    val name: String,
    val email: String,
    val role: String,
    val password: String
)

data class DeleteUserResponse(
    val success: Boolean? = true,
    val message: String? = null
)