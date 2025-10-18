package com.example.kkarhua.data.repository

import android.util.Log
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.local.ProductDao
import com.example.kkarhua.data.remote.RetrofitClient
import com.example.kkarhua.data.remote.getImageUrl
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

    suspend fun syncProductsFromApi(): Result<List<Product>> {
        return try {
            Log.d("ProductRepository", "Iniciando sincronización...")
            val response = apiService.getAllProducts()

            if (response.isSuccessful) {
                val productsResponse = response.body()
                Log.d("ProductRepository", "Response body: $productsResponse")

                if (productsResponse != null) {
                    val products = productsResponse.mapNotNull { productResponse ->
                        try {
                            val imageFromXano = productResponse.image.getImageUrl()

                            Log.d("ProductRepository", """
                                Producto: ${productResponse.name}
                                Image object: ${productResponse.image}
                                URL extraída: $imageFromXano
                            """.trimIndent())

                            Product(
                                id = productResponse.id.toString(),
                                name = productResponse.name,
                                description = productResponse.description,
                                price = productResponse.price,
                                image = imageFromXano,
                                stock = productResponse.stock
                            )
                        } catch (e: Exception) {
                            Log.e("ProductRepository", "Error procesando producto ${productResponse.name}: ${e.message}")
                            null
                        }
                    }

                    if (products.isEmpty()) {
                        Log.e("ProductRepository", "No se pudieron procesar productos")
                        return Result.failure(Exception("No se pudieron procesar los productos de la API"))
                    }

                    deleteAllProducts()
                    insertAllProducts(products)

                    Log.d("ProductRepository", "Productos sincronizados exitosamente: ${products.size}")
                    Result.success(products)
                } else {
                    Log.e("ProductRepository", "Response body es null")
                    Result.failure(Exception("No se recibieron productos"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Error en la respuesta: ${response.code()} - ${response.message()}\nBody: $errorBody"
                Log.e("ProductRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al sincronizar productos: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchProductFromApi(productId: Int): Result<Product> {
        return try {
            val response = apiService.getProductById(productId)

            if (response.isSuccessful) {
                val productResponse = response.body()

                if (productResponse != null) {
                    val imageFromXano = productResponse.image.getImageUrl()

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageFromXano,
                        stock = productResponse.stock
                    )

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