package com.example.kkarhua.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kkarhua.data.local.CartItem
import kotlinx.coroutines.flow.Flow
import com.example.kkarhua.data.local.CartDao
@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItemById(productId: String): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Métodos alternativos si prefieres estos nombres:
    @Query("SELECT * FROM cart_items")
    fun getCart(): Flow<List<CartItem>> // Alternativa

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItem) // Alternativa

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeFromCart(productId: String) // Alternativa
}