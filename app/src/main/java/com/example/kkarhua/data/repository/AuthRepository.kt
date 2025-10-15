package com.example.kkarhua.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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

    /**
     * Registrar nuevo usuario
     */
    suspend fun signup(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val request = SignupRequest(name, email, password)
            val response = authService.signup(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    saveAuthData(authResponse)
                    Log.d("AuthRepository", "Registro exitoso: ${authResponse.user.email}")
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorMsg = "Error ${response.code()}: ${response.message()}"
                Log.e("AuthRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en signup: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Iniciar sesión
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = authService.login(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    saveAuthData(authResponse)
                    Log.d("AuthRepository", "Login exitoso: ${authResponse.user.email}")
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "Usuario no encontrado"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Log.e("AuthRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login: ${e.message}", e)
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        }
    }

    /**
     * Obtener información del usuario actual
     */
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
            Log.e("AuthRepository", "Error en getMe: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Guardar datos de autenticación
     */
    private fun saveAuthData(authResponse: AuthResponse) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, authResponse.authToken)
            putInt(KEY_USER_ID, authResponse.user.id)
            putString(KEY_USER_NAME, authResponse.user.name)
            putString(KEY_USER_EMAIL, authResponse.user.email)
            apply()
        }
    }

    /**
     * Obtener token de autenticación
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Verificar si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }

    /**
     * Obtener nombre del usuario
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Obtener email del usuario
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        prefs.edit().clear().apply()
        Log.d("AuthRepository", "Sesión cerrada")
    }
}