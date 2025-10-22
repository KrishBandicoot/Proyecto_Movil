package com.example.kkarhua.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kkarhua.data.remote.*

class AuthRepository(context: Context) {

    private val authService = RetrofitClient.authService
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    suspend fun signup(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val request = SignupRequest(name, email, password)
            val response = authService.signup(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    saveAuthData(authResponse)
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Response body es null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = authService.login(request)

            if (response.isSuccessful) {
                val authResponse = response.body()

                if (authResponse != null) {
                    val token = authResponse.getToken()

                    if (token != null) {
                        saveAuthData(authResponse)
                        Result.success(authResponse)
                    } else {
                        Result.failure(Exception("Token es null"))
                    }
                } else {
                    Result.failure(Exception("Response body es null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<MeResponse> {
        return try {
            val token = getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            val response = authService.getMe("Bearer $token")

            if (response.isSuccessful) {
                val meResponse = response.body()
                if (meResponse != null) {
                    Result.success(meResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveAuthData(authResponse: AuthResponse) {
        val token = authResponse.getToken() ?: ""
        val user = authResponse.user

        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putInt(KEY_USER_ID, user?.id ?: 0)
            putString(KEY_USER_NAME, user?.name ?: "")
            putString(KEY_USER_EMAIL, user?.email ?: "")
            apply()
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}