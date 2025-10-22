package com.example.kkarhua.data.repository

import com.example.kkarhua.data.local.CartDao
import com.example.kkarhua.data.local.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems()
    }

    suspend fun getCartItemById(productId: String): CartItem? {
        return cartDao.getCartItemById(productId)
    }

    suspend fun addToCart(cartItem: CartItem) {
        val existing = cartDao.getCartItemById(cartItem.productId)
        if (existing != null) {
            // Si ya existe, actualizar cantidad
            cartDao.updateQuantity(
                cartItem.productId,
                existing.quantity + cartItem.quantity
            )
        } else {
            // Si no existe, insertar nuevo
            cartDao.insertCartItem(cartItem)
        }
    }

    suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}