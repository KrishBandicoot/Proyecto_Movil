package com.example.kkarhua.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.common.images.WebImage

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: WebImage,
    val stock: Int = 0
)