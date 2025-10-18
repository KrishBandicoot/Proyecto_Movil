package com.example.kkarhua.data.repository

import android.util.Log
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.local.ProductDao
import com.example.kkarhua.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    private val apiService = RetrofitClient.apiService

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    suspend fun getProductById(productId: String): Product? {
        return productDao.getProductById(productId)
    }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun insertAllProducts(products: List<Product>) {
        productDao.insertAllProducts(products)
    }

    suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }

    /**
     * Sincroniza los productos desde Xano API
     * Obtiene todos los productos de la API y los guarda en la base de datos local
     */
    suspend fun syncProductsFromApi(): Result<List<Product>> {
        return try {
            val response = apiService.getAllProducts()

            if (response.isSuccessful) {
                val productsResponse = response.body()

                if (productsResponse != null) {
                    // Convertir ProductResponse a Product (entidad local)
                    val products = productsResponse.map { productResponse ->
                        Product(
                            id = productResponse.id.toString(),
                            name = productResponse.name,
                            description = productResponse.description,
                            price = productResponse.price,
                            imageUrl = productResponse.imageUrl,
                            stock = productResponse.stock
                        )
                    }

                    // Limpiar base de datos y guardar nuevos productos
                    deleteAllProducts()
                    insertAllProducts(products)

                    Log.d("ProductRepository", "Productos sincronizados: ${products.size}")
                    Result.success(products)
                } else {
                    Log.e("ProductRepository", "Response body es null")
                    Result.failure(Exception("No se recibieron productos"))
                }
            } else {
                val errorMsg = "Error en la respuesta: ${response.code()} - ${response.message()}"
                Log.e("ProductRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al sincronizar productos: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un producto específico desde la API
     */
    suspend fun fetchProductFromApi(productId: Int): Result<Product> {
        return try {
            val response = apiService.getProductById(productId)

            if (response.isSuccessful) {
                val productResponse = response.body()

                if (productResponse != null) {
                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        imageUrl = productResponse.imageUrl,
                        stock = productResponse.stock
                    )

                    // Actualizar en la base de datos local
                    insertProduct(product)

                    Result.success(product)
                } else {
                    Result.failure(Exception("Producto no encontrado"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al obtener producto: ${e.message}", e)
            Result.failure(e)
        }
    }
}