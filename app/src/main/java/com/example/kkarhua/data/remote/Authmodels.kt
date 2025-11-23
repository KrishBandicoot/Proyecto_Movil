package com.example.kkarhua.data.remote

import com.google.gson.annotations.SerializedName

// Request models
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
    // ✅ NO enviamos role - Xano lo asigna automáticamente con el default "member"
)

data class LoginRequest(
    val email: String,
    val password: String
)

// Response models
data class AuthResponse(
    @SerializedName("authToken")
    val authToken: String? = null,

    @SerializedName("auth_token")
    val authTokenAlternative: String? = null,

    @SerializedName("user")
    val user: UserData? = null
) {
    fun getToken(): String? = authToken ?: authTokenAlternative
}

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String? = "member", // ✅ Rol del usuario

    @SerializedName("created_at")
    val created_at: Long? = null
)

data class MeResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String? = "member", // ✅ Rol del usuario

    @SerializedName("created_at")
    val created_at: Long? = null
)

// Error response model
data class ErrorResponse(
    val message: String? = null,
    val error: String? = null
)