package com.example.kkarhua.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.kkarhua.data.local.CartItem
class CartViewModel(private val repo: CartRepository) : ViewModel() {
    val cartItems = repo.getCartItems().asLiveData()

    fun addProduct(id: Long) = viewModelScope.launch {
        repo.addToCart(CartItem(id, 1))
    }

    fun clearCart() = viewModelScope.launch { repo.clearCart() }
}
