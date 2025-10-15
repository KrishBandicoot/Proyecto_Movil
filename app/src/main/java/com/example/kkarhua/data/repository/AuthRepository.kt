package com.example.kkarhua.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.kkarhua.data.remote.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class AuthRepository(context: Context) {

    private val authService = RetrofitClient.authService
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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
            Log.d("AuthRepository", "Enviando registro: $request")

            val response = authService.signup(request)

            Log.d("AuthRepository", "Código de respuesta: ${response.code()}")
            Log.d("AuthRepository", "Respuesta exitosa: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val authResponse = response.body()

                if (authResponse != null) {
                    // Validar que los campos importantes no sean null
                    if (authResponse.authToken == null) {
                        Log.e("AuthRepository", "authToken es null en la respuesta")
                        return Result.failure(Exception("Error: Token de autenticación no recibido"))
                    }

                    if (authResponse.user == null) {
                        Log.e("AuthRepository", "user es null en la respuesta")
                        return Result.failure(Exception("Datos ingresados correctamente"))
                    }

                    saveAuthData(authResponse)
                    Log.d("AuthRepository", "Registro exitoso: ${authResponse.user?.email}")
                    Result.success(authResponse)
                } else {
                    Log.e("AuthRepository", "Response body es null")
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                // Intentar leer el error del body
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Error body: $errorBody")

                val errorMsg = try {
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message ?: errorResponse.error ?: "Error desconocido"
                } catch (e: JsonSyntaxException) {
                    when (response.code()) {
                        400 -> "Datos inválidos. Verifica los campos"
                        403 -> "Acceso denegado. Verifica la configuración de la API"
                        409 -> "El correo ya está registrado"
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                }

                Log.e("AuthRepository", "Error en signup: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Excepción en signup: ${e.message}", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Iniciar sesión
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            Log.d("AuthRepository", "Enviando login: $email")

            val response = authService.login(request)

            Log.d("AuthRepository", "Código de respuesta login: ${response.code()}")

            if (response.isSuccessful) {
                val authResponse = response.body()

                if (authResponse != null) {
                    // Validar que los campos importantes no sean null
                    if (authResponse.authToken == null) {
                        Log.e("AuthRepository", "authToken es null en la respuesta de login")
                        return Result.failure(Exception("Error: Token de autenticación no recibido"))
                    }

                    if (authResponse.user == null) {
                        Log.e("AuthRepository", "user es null en la respuesta de login")
                        return Result.failure(Exception("Datos ingresados correctamente"))
                    }

                    saveAuthData(authResponse)
                    Log.d("AuthRepository", "Login exitoso: ${authResponse.user?.email}")
                    Result.success(authResponse)
                } else {
                    Log.e("AuthRepository", "Response body es null en login")
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Error body login: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "Usuario no encontrado"
                    403 -> "Acceso denegado"
                    else -> "Error ${response.code()}: ${response.message()}"
                }

                Log.e("AuthRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Excepción en login: ${e.message}", e)
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
        val token = authResponse.authToken ?: ""
        val user = authResponse.user

        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putInt(KEY_USER_ID, user?.id ?: 0)
            putString(KEY_USER_NAME, user?.name ?: "")
            putString(KEY_USER_EMAIL, user?.email ?: "")
            apply()
        }

        Log.d("AuthRepository", "Datos guardados - Token: ${token.take(10)}..., User: ${user?.name}")
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