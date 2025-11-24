package com.example.kkarhua.data.remote

import retrofit2.Response
import retrofit2.http.*

interface UserService {

    @GET("users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<UserResponse>

    @PATCH("users/{id}")
    @Headers("Content-Type: application/json")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body userData: UpdateUserData
    ): Response<UserResponse>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<Unit>
}

// Data classes para usuarios
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val created_at: Long?
)

data class UpdateUserData(
    val name: String?,
    val email: String?,
    val role: String?,
    val password: String? = null
)