package com.example.kkarhua.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    val products: LiveData<List<Product>> = repository.getAllProducts().asLiveData()

    init {
        // Insertar datos de muestra al iniciar
        loadSampleData()
    }

    private fun loadSampleData() = viewModelScope.launch {
        repository.insertSampleData()
    }

    fun getProductById(productId: String) = viewModelScope.launch {
        repository.getProductById(productId)
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