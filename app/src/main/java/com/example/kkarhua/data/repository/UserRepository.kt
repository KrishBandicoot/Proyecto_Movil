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

            val response = userService.getAllUsers("Bearer $token")

            if (response.isSuccessful) {
                val users = response.body()
                if (users != null) {
                    Log.d(TAG, "✓ Usuarios obtenidos: ${users.size}")
                    Result.success(users)
                } else {
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

            val response = userService.getUserById("Bearer $token", userId)

            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Usuario no encontrado"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(
        userId: Int,
        name: String?,
        email: String?,
        role: String?,
        password: String?
    ): Result<UserResponse> {
        return try {
            val token = authRepository.getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            val updateData = UpdateUserData(
                name = name,
                email = email,
                role = role,
                password = password
            )

            val response = userService.updateUser("Bearer $token", userId, updateData)

            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Log.d(TAG, "✓ Usuario actualizado: ${user.name}")
                    Result.success(user)
                } else {
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

    suspend fun deleteUser(userId: Int): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken()
            if (token == null) {
                return Result.failure(Exception("No hay sesión activa"))
            }

            Log.d(TAG, "→ Eliminando usuario ID: $userId")
            Log.d(TAG, "→ Token: ${token.take(20)}...")

            val response = userService.deleteUser("Bearer $token", userId)

            Log.d(TAG, "← Response code: ${response.code()}")

            if (response.isSuccessful) {
                Log.d(TAG, "✓ Usuario eliminado")
                Result.success(Unit)
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
}