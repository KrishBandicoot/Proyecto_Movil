package com.example.kkarhua.data.repository

import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.local.ProductDao
import com.example.kkarhua.data.remote.RetrofitClient
import com.example.kkarhua.data.remote.getImageUrl
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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
            val response = apiService.getAllProducts()

            if (response.isSuccessful) {
                val productsResponse = response.body()

                if (productsResponse != null) {
                    val products = productsResponse.mapNotNull { productResponse ->
                        try {
                            val imageUrl = productResponse.image.getImageUrl()

                            Product(
                                id = productResponse.id.toString(),
                                name = productResponse.name,
                                description = productResponse.description,
                                price = productResponse.price,
                                image = imageUrl,
                                stock = productResponse.stock
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (products.isEmpty()) {
                        return Result.failure(Exception("No se pudieron procesar productos"))
                    }

                    deleteAllProducts()
                    insertAllProducts(products)

                    Result.success(products)
                } else {
                    Result.failure(Exception("Response body es null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProductFromApi(productId: Int): Result<Product> {
        return try {
            val response = apiService.getProductById(productId)

            if (response.isSuccessful) {
                val productResponse = response.body()

                if (productResponse != null) {
                    val imageUrl = productResponse.image.getImageUrl()

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageUrl,
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
            Result.failure(e)
        }
    }

    suspend fun createProductInApi(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        imageFile: File
    ): Result<Product> {
        return try {
            if (!imageFile.exists()) {
                return Result.failure(Exception("Archivo no existe"))
            }

            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestFile
            )

            val response = apiService.createProduct(
                name = nameBody,
                description = descriptionBody,
                price = priceBody,
                stock = stockBody,
                image = imagePart
            )

            when {
                response.isSuccessful && response.body() != null -> {
                    val productResponse = response.body()!!
                    val imageUrl = productResponse.image.getImageUrl()

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageUrl,
                        stock = productResponse.stock
                    )

                    insertProduct(product)
                    Result.success(product)
                }
                else -> {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ NUEVO: Actualizar producto en la API
    suspend fun updateProductInApi(
        productId: Int,
        name: String,
        description: String,
        price: Double,
        stock: Int,
        imageFile: File?  // Null si no se cambia la imagen
    ): Result<Product> {
        return try {
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart: MultipartBody.Part? = imageFile?.let {
                if (it.exists()) {
                    val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", it.name, requestFile)
                } else {
                    null
                }
            }

            val response = apiService.updateProduct(
                id = productId,
                name = nameBody,
                description = descriptionBody,
                price = priceBody,
                stock = stockBody,
                image = imagePart
            )

            when {
                response.isSuccessful && response.body() != null -> {
                    val productResponse = response.body()!!
                    val imageUrl = productResponse.image.getImageUrl()

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageUrl,
                        stock = productResponse.stock
                    )

                    insertProduct(product)
                    Result.success(product)
                }
                else -> {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ NUEVO: Eliminar producto de la API
    suspend fun deleteProductFromApi(productId: Int): Result<Unit> {
        return try {
            val response = apiService.deleteProduct(productId)

            if (response.isSuccessful) {
                deleteProduct(productId.toString())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}