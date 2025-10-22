package com.example.kkarhua.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val image: String  // ✅ CORREGIDO: String para almacenar la URL de Xano
)