package com.example.kkarhua.data.repository

import android.util.Log
import com.example.kkarhua.data.remote.RetrofitClient
import com.example.kkarhua.data.remote.UpdateUserData
import com.example.kkarhua.data.remote.UserResponse

class UserRepository(private val authRepository: AuthRepository) {

    private val userService = RetrofitClient.userService
    private val TAG = "UserRepository"

    suspend fun getAllUsers(): Result<List<UserResponse>> {
        return try {
            val token = authRepository.getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            Log.d(TAG, "→ Obteniendo usuarios")
            Log.d(TAG, "  Token: ${token.take(20)}...")
            Log.d(TAG, "  Intentando endpoint: /users")

            val response = userService.getAllUsers(token)

            Log.d(TAG, "← Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body()
                if (users != null) {
                    Log.d(TAG, "✓ Usuarios obtenidos: ${users.size}")
                    Result.success(users)
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

    suspend fun getUserById(userId: Int): Result<UserResponse> {
        return try {
            val token = authRepository.getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            Log.d(TAG, "→ Obteniendo usuario ID: $userId")
            Log.d(TAG, "  Intentando endpoint: /users/$userId")

            val response = userService.getUserById(token, userId)

            Log.d(TAG, "← Response code: ${response.code()}")

            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Log.d(TAG, "✓ Usuario obtenido: ${user.name}")
                    Result.success(user)
                } else {
                    Log.e(TAG, "✗ Usuario no encontrado")
                    Result.failure(Exception("Usuario no encontrado"))
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

    suspend fun updateUser(
        userId: Int,
        name: String,
        email: String,
        role: String
    ): Result<UserResponse> {
        return try {
            val token = authRepository.getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            Log.d(TAG, "========================================")
            Log.d(TAG, "UPDATE USER")
            Log.d(TAG, "========================================")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Endpoint: /users/$userId")
            Log.d(TAG, "Name: $name")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Role: $role")
            Log.d(TAG, "Token: ${token.take(20)}...")
            Log.d(TAG, "========================================")

            // ✅ CORREGIDO: Estructura de datos simplificada
            val updateData = UpdateUserData(
                name = name,
                email = email,
                role = role
            )

            Log.d(TAG, "Update data: $updateData")

            val response = userService.updateUser(token, userId, updateData)

            Log.d(TAG, "========================================")
            Log.d(TAG, "RESPONSE")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")

            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Log.d(TAG, "✓ Usuario actualizado: ${user.name}")
                    Log.d(TAG, "  Email: ${user.email}")
                    Log.d(TAG, "  Role: ${user.role}")
                    Log.d(TAG, "========================================")
                    Result.success(user)
                } else {
                    Log.e(TAG, "✗ Response body es null")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("Response body es null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Error body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "EXCEPTION: ${e.message}", e)
            Log.e(TAG, "========================================")
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Int): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            Log.d(TAG, "========================================")
            Log.d(TAG, "DELETE USER")
            Log.d(TAG, "========================================")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Endpoint: /users/$userId")
            Log.d(TAG, "Token: ${token.take(20)}...")
            Log.d(TAG, "========================================")

            val response = userService.deleteUser(token, userId)

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")

            if (response.isSuccessful) {
                Log.d(TAG, "✓ Usuario eliminado exitosamente")
                Log.d(TAG, "========================================")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Error body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "EXCEPTION: ${e.message}", e)
            Log.e(TAG, "========================================")
            Result.failure(e)
        }
    }
}