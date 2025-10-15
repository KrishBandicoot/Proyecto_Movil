package com.example.kkarhua.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kkarhua.data.local.CartItem
import com.example.kkarhua.data.repository.CartRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    val cartItems: LiveData<List<CartItem>> = repository.getCartItems().asLiveData()

    val totalPrice: LiveData<Double> = repository.getCartItems()
        .map { items -> items.sumOf { it.price * it.quantity } }
        .asLiveData()

    val itemCount: LiveData<Int> = repository.getCartItems()
        .map { it.size }
        .asLiveData()

    fun addToCart(cartItem: CartItem) = viewModelScope.launch {
        repository.addToCart(cartItem)
    }

    fun updateQuantity(productId: String, quantity: Int) = viewModelScope.launch {
        repository.updateQuantity(productId, quantity)
    }

    fun removeFromCart(productId: String) = viewModelScope.launch {
        repository.removeFromCart(productId)
    }

    fun clearCart() = viewModelScope.launch {
        repository.clearCart()
    }
}

class CartViewModelFactory(
    private val repository: CartRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}