package com.example.kkarhua.viewmodel
class ProductListViewModel(private val repo: ProductRepository) : ViewModel() {
    val products: LiveData<List<Product>> = repo.getAllProducts().asLiveData()
}
