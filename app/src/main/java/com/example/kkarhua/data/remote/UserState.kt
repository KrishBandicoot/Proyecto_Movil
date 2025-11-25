package com.example.kkarhua.data.remote

import com.google.gson.annotations.SerializedName

enum class UserState(val value: String) {
    ACTIVO("activo"),
    BLOQUEADO("bloqueado");

    companion object {
        fun fromString(value: String?): UserState {
            return when (value?.lowercase()) {
                "bloqueado" -> BLOQUEADO
                else -> ACTIVO // Default
            }
        }
    }
}

// Request models
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val state: String = "activo" // ✅ NUEVO: Default activo
)

data class LoginRequest(
    val email: String,
    val password: String
)

// ✅ ACTUALIZADO: Incluye state con default
data class AdminSignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val state: String = "activo" // ✅ NUEVO: Default activo
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
    val role: String? = "member",
    val state: String? = "activo", // ✅ NUEVO

    @SerializedName("created_at")
    val created_at: Long? = null
)

data class MeResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String? = "member",
    val state: String? = "activo", // ✅ NUEVO

    @SerializedName("created_at")
    val created_at: Long? = null
)

// Error response model
data class ErrorResponse(
    val message: String? = null,
    val error: String? = null
)