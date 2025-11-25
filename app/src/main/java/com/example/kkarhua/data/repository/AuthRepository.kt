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
        private const val TAG = "AuthRepository"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_STATE = "user_state" // ✅ NUEVO
    }

    suspend fun signup(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val request = SignupRequest(name, email, password) // Ya incluye state = "activo"
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

    suspend fun adminSignup(name: String, email: String, password: String, role: String): Result<AuthResponse> {
        return try {
            val token = getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión de administrador activa"))
            }

            val request = AdminSignupRequest(name, email, password, role) // Ya incluye state = "activo"
            val response = authService.adminSignup("Bearer $token", request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d(TAG, "✓ Usuario creado por admin exitosamente")
                    Log.d(TAG, "  - Name: ${authResponse.user?.name}")
                    Log.d(TAG, "  - Email: ${authResponse.user?.email}")
                    Log.d(TAG, "  - Role: ${authResponse.user?.role}")
                    Log.d(TAG, "  - State: ${authResponse.user?.state}")
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Response body es null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Error al crear usuario: $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception en adminSignup: ${e.message}", e)
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
                        Log.d(TAG, "✓ Login exitoso - Token obtenido")

                        // ✅ Guardar token primero
                        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()

                        // ✅ Obtener datos completos del usuario con /auth/me
                        Log.d(TAG, "→ Obteniendo datos del usuario con /auth/me")
                        val meResult = getMe()

                        if (meResult.isSuccess) {
                            val meResponse = meResult.getOrNull()
                            Log.d(TAG, "✓ Datos del usuario obtenidos:")
                            Log.d(TAG, "  - ID: ${meResponse?.id}")
                            Log.d(TAG, "  - Name: ${meResponse?.name}")
                            Log.d(TAG, "  - Email: ${meResponse?.email}")
                            Log.d(TAG, "  - Role: ${meResponse?.role}")
                            Log.d(TAG, "  - State: ${meResponse?.state}")

                            // ✅ NUEVO: Verificar si el usuario está bloqueado
                            val userState = meResponse?.state ?: "activo"
                            if (userState == "bloqueado") {
                                Log.e(TAG, "✗ Usuario bloqueado")
                                logout() // Limpiar sesión
                                return Result.failure(Exception("Tu cuenta ha sido bloqueada. Contacta al administrador."))
                            }

                            // Crear AuthResponse completo con los datos de /auth/me
                            val completeAuthResponse = AuthResponse(
                                authToken = token,
                                user = UserData(
                                    id = meResponse?.id ?: 0,
                                    name = meResponse?.name ?: "",
                                    email = meResponse?.email ?: "",
                                    role = meResponse?.role ?: "member",
                                    state = userState, // ✅ NUEVO
                                    created_at = meResponse?.created_at
                                )
                            )

                            saveAuthData(completeAuthResponse)
                            Result.success(completeAuthResponse)
                        } else {
                            Log.e(TAG, "✗ Error al obtener datos del usuario")
                            Result.failure(Exception("Error al obtener datos del usuario"))
                        }
                    } else {
                        Log.e(TAG, "✗ Token es null")
                        Result.failure(Exception("Token es null"))
                    }
                } else {
                    Log.e(TAG, "✗ Response body es null")
                    Result.failure(Exception("Response body es null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Error ${response.code()}: $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<MeResponse> {
        return try {
            val token = getAuthToken()
            if (token == null) {
                Log.e(TAG, "✗ No hay token para /auth/me")
                return Result.failure(Exception("No hay sesión activa"))
            }

            Log.d(TAG, "→ Llamando a /auth/me con token")
            val response = authService.getMe("Bearer $token")

            if (response.isSuccessful) {
                val meResponse = response.body()
                if (meResponse != null) {
                    Log.d(TAG, "✓ /auth/me exitoso:")
                    Log.d(TAG, "  - ID: ${meResponse.id}")
                    Log.d(TAG, "  - Name: ${meResponse.name}")
                    Log.d(TAG, "  - Email: ${meResponse.email}")
                    Log.d(TAG, "  - Role: ${meResponse.role}")
                    Log.d(TAG, "  - State: ${meResponse.state}")

                    // Actualizar datos en SharedPreferences
                    prefs.edit().apply {
                        putInt(KEY_USER_ID, meResponse.id)
                        putString(KEY_USER_NAME, meResponse.name)
                        putString(KEY_USER_EMAIL, meResponse.email)
                        putString(KEY_USER_ROLE, meResponse.role ?: "member")
                        putString(KEY_USER_STATE, meResponse.state ?: "activo") // ✅ NUEVO
                        apply()
                    }

                    Result.success(meResponse)
                } else {
                    Log.e(TAG, "✗ /auth/me response body es null")
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ /auth/me error ${response.code()}: $errorBody")
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception en /auth/me: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun saveAuthData(authResponse: AuthResponse) {
        val token = authResponse.getToken() ?: ""
        val user = authResponse.user
        val role = user?.role ?: "member"
        val state = user?.state ?: "activo" // ✅ NUEVO

        Log.d(TAG, "========================================")
        Log.d(TAG, "GUARDANDO DATOS DE USUARIO")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Token: ${if (token.isNotEmpty()) "✓ Presente" else "✗ Ausente"}")
        Log.d(TAG, "User ID: ${user?.id}")
        Log.d(TAG, "User Name: ${user?.name}")
        Log.d(TAG, "User Email: ${user?.email}")
        Log.d(TAG, "User Role: '$role'")
        Log.d(TAG, "User State: '$state'") // ✅ NUEVO
        Log.d(TAG, "========================================")

        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putInt(KEY_USER_ID, user?.id ?: 0)
            putString(KEY_USER_NAME, user?.name ?: "")
            putString(KEY_USER_EMAIL, user?.email ?: "")
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_STATE, state) // ✅ NUEVO
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

    fun getUserRole(): String {
        return prefs.getString(KEY_USER_ROLE, "member") ?: "member"
    }

    // ✅ NUEVO: Obtener estado del usuario
    fun getUserState(): String {
        return prefs.getString(KEY_USER_STATE, "activo") ?: "activo"
    }

    // ✅ NUEVO: Verificar si está bloqueado
    fun isBlocked(): Boolean {
        return getUserState() == "bloqueado"
    }

    fun isAdmin(): Boolean {
        val role = getUserRole()
        return role == "admin"
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}