package com.example.kkarhua.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: String,  // ✅ CORREGIDO: String para almacenar la URL de Xano
    val stock: Int = 0
)