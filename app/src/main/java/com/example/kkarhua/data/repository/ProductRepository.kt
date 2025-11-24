package com.example.kkarhua.data.repository

import android.util.Log
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
    private val TAG = "ProductRepository"

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
                                stock = productResponse.stock,
                                category = productResponse.category
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
                        stock = productResponse.stock,
                        category = productResponse.category
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
        category: String,
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
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

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
                category = categoryBody,
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
                        stock = productResponse.stock,
                        category = productResponse.category
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



    suspend fun updateProductInApi(
        productId: Int,
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        imageFile: File?
    ): Result<Product> {
        return try {
            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "UPDATE PRODUCT - INICIO")
            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "Product ID: $productId")
            Log.d(TAG, "Name: $name")
            Log.d(TAG, "Has image to upload: ${imageFile != null && imageFile.exists()}")
            Log.d(TAG, "════════════════════════════════════════")

            // Si hay imagen nueva, primero crear un producto temporal para obtener la URL de Xano
            val imageData: com.example.kkarhua.data.remote.ImageUpdateData? = if (imageFile != null && imageFile.exists()) {
                Log.d(TAG, "→ Subiendo imagen nueva a Xano...")

                // Crear producto temporal para subir la imagen
                val tempNameBody = "TEMP_${System.currentTimeMillis()}".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempDescBody = "temp".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempPriceBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempStockBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempCategoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                // Crear producto temporal
                val tempResponse = apiService.createProduct(
                    name = tempNameBody,
                    description = tempDescBody,
                    price = tempPriceBody,
                    stock = tempStockBody,
                    category = tempCategoryBody,
                    image = imagePart
                )

                if (tempResponse.isSuccessful && tempResponse.body() != null) {
                    val tempProduct = tempResponse.body()!!
                    val tempId = tempProduct.id
                    val imageObject = tempProduct.image

                    Log.d(TAG, "✓ Imagen subida correctamente")
                    Log.d(TAG, "  Temp product ID: $tempId")
                    Log.d(TAG, "  Image data: $imageObject")

                    // Eliminar producto temporal
                    try {
                        apiService.deleteProduct(tempId)
                        Log.d(TAG, "✓ Producto temporal eliminado")
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo eliminar producto temporal: ${e.message}")
                    }

                    // Retornar el objeto image completo
                    com.example.kkarhua.data.remote.ImageUpdateData(
                        path = imageObject?.path ?: "",
                        name = imageObject?.name ?: "",
                        type = imageObject?.type ?: "",
                        size = imageObject?.size ?: 0,
                        mime = imageObject?.mime ?: "",
                        url = imageObject?.url ?: "",
                        meta = com.example.kkarhua.data.remote.ImageMetaUpdate(
                            width = imageObject?.meta?.width ?: 0,
                            height = imageObject?.meta?.height ?: 0
                        )
                    )
                } else {
                    Log.e(TAG, "✗ Error subiendo imagen: ${tempResponse.code()}")
                    null
                }
            } else {
                Log.d(TAG, "→ Sin imagen nueva, manteniendo imagen actual")
                null
            }

            // Crear el objeto de datos para actualizar
            val updateData = com.example.kkarhua.data.remote.UpdateProductData(
                name = name,
                description = description,
                price = price,
                stock = stock,
                category = category,
                image = imageData  // null si no hay imagen nueva
            )

            if (imageData != null) {
                Log.d(TAG, "→ Actualizando producto CON imagen nueva")
            } else {
                Log.d(TAG, "→ Actualizando producto SIN cambiar imagen")
            }

            Log.d(TAG, "Datos a enviar: $updateData")

            // Actualizar el producto
            val response = apiService.updateProduct(
                id = productId,
                productData = updateData
            )

            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "RESPUESTA DE API")
            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")
            Log.d(TAG, "Is successful: ${response.isSuccessful}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "════════════════════════════════════════")
                return Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }

            val productResponse = response.body()
            if (productResponse == null) {
                Log.e(TAG, "Response body es null")
                Log.d(TAG, "════════════════════════════════════════")
                return Result.failure(Exception("Response body es null"))
            }

            Log.d(TAG, "✓ Producto actualizado correctamente")
            Log.d(TAG, "  ID: ${productResponse.id}")
            Log.d(TAG, "  Name: ${productResponse.name}")
            Log.d(TAG, "  Image URL: ${productResponse.image.getImageUrl()}")
            Log.d(TAG, "════════════════════════════════════════")

            val imageUrl = productResponse.image.getImageUrl()

            val product = Product(
                id = productResponse.id.toString(),
                name = productResponse.name,
                description = productResponse.description,
                price = productResponse.price,
                image = imageUrl,
                stock = productResponse.stock,
                category = productResponse.category
            )

            insertProduct(product)
            Result.success(product)

        } catch (e: Exception) {
            Log.e(TAG, "════════════════════════════════════════")
            Log.e(TAG, "EXCEPCIÓN: ${e.message}", e)
            Log.e(TAG, "════════════════════════════════════════")
            Result.failure(e)
        }
    }

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