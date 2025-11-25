package com.example.kkarhua.data.remote

import retrofit2.Response
import retrofit2.http.*

interface PurchaseService {

    // ============================================
    // ADDRESS ENDPOINTS
    // ============================================

    @POST("address")
    @Headers("Content-Type: application/json")
    suspend fun createAddress(
        @Header("Authorization") token: String,
        @Body address: AddressRequest
    ): Response<AddressResponse>

    @GET("address/{id}")
    suspend fun getAddressById(
        @Header("Authorization") token: String,
        @Path("id") addressId: Int
    ): Response<AddressResponse>

    @GET("address")
    suspend fun getUserAddresses(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int
    ): Response<List<AddressResponse>>

    // ============================================
    // PURCHASE ENDPOINTS
    // ============================================

    @POST("purchase")
    @Headers("Content-Type: application/json")
    suspend fun createPurchase(
        @Header("Authorization") token: String,
        @Body purchase: PurchaseRequest
    ): Response<PurchaseResponse>

    @GET("purchase/{id}")
    suspend fun getPurchaseById(
        @Header("Authorization") token: String,
        @Path("id") purchaseId: Int
    ): Response<PurchaseResponse>

    @GET("purchase")
    suspend fun getAllPurchases(
        @Header("Authorization") token: String
    ): Response<List<PurchaseResponse>>

    @GET("purchase")
    suspend fun getUserPurchases(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int
    ): Response<List<PurchaseResponse>>

    @PATCH("purchase/{id}")
    @Headers("Content-Type: application/json")
    suspend fun updatePurchaseStatus(
        @Header("Authorization") token: String,
        @Path("id") purchaseId: Int,
        @Body statusUpdate: Map<String, String>
    ): Response<PurchaseResponse>

    // ============================================
    // PURCHASE ITEM ENDPOINTS
    // ============================================

    @POST("purchase_item")
    @Headers("Content-Type: application/json")
    suspend fun createPurchaseItem(
        @Header("Authorization") token: String,
        @Body item: PurchaseItemRequest
    ): Response<PurchaseItemResponse>

    @GET("purchase_item")
    suspend fun getPurchaseItems(
        @Header("Authorization") token: String,
        @Query("purchase_id") purchaseId: Int
    ): Response<List<PurchaseItemResponse>>
}