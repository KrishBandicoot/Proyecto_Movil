package com.example.kkarhua.data.repository

import android.util.Log
import com.example.kkarhua.data.remote.*

class PurchaseRepository(private val authRepository: AuthRepository) {

    private val addressService = RetrofitClient.addressService
    private val purchaseService = RetrofitClient.purchaseService
    private val purchaseItemService = RetrofitClient.purchaseItemService

    companion object {
        private const val TAG = "PurchaseRepository"
    }

    // ============================================
    // ADDRESS METHODS
    // ============================================

    suspend fun createAddress(
        addressLine1: String,
        apartmentNumber: String,
        region: String,
        commune: String,
        shippingInstructions: String,
        userId: Int
    ): Result<AddressResponse> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val request = AddressRequest(
                address_line_1 = addressLine1,
                apartment_number = apartmentNumber,
                region = region,
                commune = commune,
                shipping_instructions = shippingInstructions,
                user_id = userId
            )

            Log.d(TAG, "→ Creando dirección para user $userId")
            val response = addressService.createAddress("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Dirección creada: ID ${response.body()!!.id}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al crear dirección: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================
    // PURCHASE METHODS
    // ============================================

    suspend fun createPurchase(
        userId: Int,
        addressId: Int,
        totalAmount: Double
    ): Result<PurchaseResponse> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            // ✅ FIX: Cambiar a "pendiente" (minúscula) según Xano
            val request = PurchaseRequest(
                user_id = userId,
                address_id = addressId,
                total_amount = totalAmount,
                status = "pendiente"  // ✅ CAMBIADO A MINÚSCULA
            )

            Log.d(TAG, "→ Creando compra para user $userId")
            Log.d(TAG, "  Total: $$totalAmount")
            Log.d(TAG, "  Status enviado: 'pendiente'")
            val response = purchaseService.createPurchase("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Compra creada: ID ${response.body()!!.id}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al crear compra: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserPurchases(userId: Int): Result<List<PurchaseResponse>> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            Log.d(TAG, "→ Obteniendo compras del user $userId")
            val response = purchaseService.getUserPurchases("Bearer $token", userId)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Compras obtenidas: ${response.body()!!.size}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al obtener compras: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllPurchases(): Result<List<PurchaseResponse>> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            Log.d(TAG, "→ Obteniendo todas las compras (admin)")
            val response = purchaseService.getAllPurchases("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Compras obtenidas: ${response.body()!!.size}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al obtener compras: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updatePurchaseStatus(
        purchaseId: Int,
        status: String
    ): Result<PurchaseResponse> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val statusMap = mapOf("status" to status)

            Log.d(TAG, "→ Actualizando estado de compra $purchaseId a: $status")
            val response = purchaseService.updatePurchaseStatus(
                "Bearer $token",
                purchaseId,
                statusMap
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Estado actualizado correctamente")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al actualizar estado: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================
    // PURCHASE ITEM METHODS
    // ============================================

    suspend fun createPurchaseItem(
        purchaseId: Int,
        productId: Int,
        quantity: Int,
        priceAtPurchase: Double
    ): Result<PurchaseItemResponse> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val request = PurchaseItemRequest(
                purchase_id = purchaseId,
                product_id = productId,
                quantity = quantity,
                price_at_purchase = priceAtPurchase
            )

            Log.d(TAG, "→ Creando item para compra $purchaseId")
            val response = purchaseItemService.createPurchaseItem("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Item creado: ID ${response.body()!!.id}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al crear item: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPurchaseItems(purchaseId: Int): Result<List<PurchaseItemResponse>> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            Log.d(TAG, "→ Obteniendo items de compra $purchaseId")
            val response = purchaseItemService.getPurchaseItems("Bearer $token", purchaseId)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Items obtenidos: ${response.body()!!.size}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "✗ Error: $error")
                Result.failure(Exception("Error al obtener items: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAddressById(addressId: Int): Result<AddressResponse> {
        return try {
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val response = addressService.getAddressById("Bearer $token", addressId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener dirección"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}