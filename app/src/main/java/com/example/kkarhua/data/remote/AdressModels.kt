package com.example.kkarhua.data.remote

import com.google.gson.annotations.SerializedName

// ============================================
// ADDRESS MODELS
// ============================================

data class AddressRequest(
    val address_line_1: String,
    val apartment_number: String,
    val region: String,
    val commune: String,
    val shipping_instructions: String,
    val user_id: Int
)

data class AddressResponse(
    val id: Int,
    @SerializedName("created_at")
    val createdAt: Long,
    val address_line_1: String,
    val apartment_number: String,
    val region: String,
    val commune: String,
    val shipping_instructions: String,
    val user_id: Int
)

// ============================================
// PURCHASE MODELS
// ============================================

data class PurchaseRequest(
    val user_id: Int,
    val address_id: Int,
    val total_amount: Double,
    val status: String = "pending" // pending, approved, rejected
)

data class PurchaseResponse(
    val id: Int,
    @SerializedName("created_at")
    val createdAt: Long,
    val user_id: Int,
    val address_id: Int,
    val total_amount: Double,
    val status: String
)

// ============================================
// PURCHASE ITEM MODELS
// ============================================

data class PurchaseItemRequest(
    val purchase_id: Int,
    val product_id: Int,
    val quantity: Int,
    val price_at_purchase: Double
)

data class PurchaseItemResponse(
    val id: Int,
    @SerializedName("created_at")
    val createdAt: Long,
    val purchase_id: Int,
    val product_id: Int,
    val quantity: Int,
    val price_at_purchase: Double
)

// ============================================
// COMBINED MODELS FOR UI
// ============================================

data class PurchaseWithDetails(
    val purchase: PurchaseResponse,
    val address: AddressResponse?,
    val items: List<PurchaseItemWithProduct>
)

data class PurchaseItemWithProduct(
    val item: PurchaseItemResponse,
    val productName: String,
    val productImage: String
)

// ============================================
// ENUMS
// ============================================

enum class PurchaseStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromString(value: String?): PurchaseStatus {
            return when (value?.lowercase()) {
                "approved" -> APPROVED
                "rejected" -> REJECTED
                else -> PENDING
            }
        }
    }
}

enum class ChileanRegion(val displayName: String) {
    ARICA_PARINACOTA("Arica y Parinacota"),
    TARAPACA("Tarapacá"),
    ANTOFAGASTA("Antofagasta"),
    ATACAMA("Atacama"),
    COQUIMBO("Coquimbo"),
    VALPARAISO("Valparaíso"),
    METROPOLITANA("Región Metropolitana"),
    OHIGGINS("O'Higgins"),
    MAULE("Maule"),
    NUBLE("Ñuble"),
    BIOBIO("Biobío"),
    ARAUCANIA("Araucanía"),
    LOS_RIOS("Los Ríos"),
    LOS_LAGOS("Los Lagos"),
    AYSEN("Aysén"),
    MAGALLANES("Magallanes");

    companion object {
        fun getAllRegions(): List<String> {
            return values().map { it.displayName }
        }
    }
}