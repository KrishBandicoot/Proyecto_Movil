package com.example.kkarhua.data.remote

// Request models
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// Response models
data class AuthResponse(
    val authToken: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val created_at: Long? = null
)

data class MeResponse(
    val id: Int,
    val name: String,
    val email: String,
    val created_at: Long? = null
)