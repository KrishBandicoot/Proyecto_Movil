package com.example.kkarhua.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    val products: LiveData<List<Product>> = repository.getAllProducts().asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _syncSuccess = MutableLiveData<Boolean>()
    val syncSuccess: LiveData<Boolean> = _syncSuccess

    init {
        syncProducts()
    }

    fun syncProducts() = viewModelScope.launch {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val result = repository.syncProductsFromApi()

            result.onSuccess {
                _syncSuccess.value = true
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = "Error al cargar productos: ${exception.message}"
                _syncSuccess.value = false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error inesperado: ${e.message}"
            _syncSuccess.value = false
        } finally {
            _isLoading.value = false
        }
    }

    fun getProductById(productId: String) = viewModelScope.launch {
        repository.getProductById(productId)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class ProductListViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}