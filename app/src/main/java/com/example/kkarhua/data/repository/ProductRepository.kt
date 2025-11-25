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
                            val imageUrl2 = productResponse.image2.getImageUrl() // ✅ NUEVO
                            val imageUrl3 = productResponse.image3.getImageUrl() // ✅ NUEVO

                            Product(
                                id = productResponse.id.toString(),
                                name = productResponse.name,
                                description = productResponse.description,
                                price = productResponse.price,
                                image = imageUrl,
                                image2 = imageUrl2, // ✅ NUEVO
                                image3 = imageUrl3, // ✅ NUEVO
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
                    val imageUrl2 = productResponse.image2.getImageUrl() // ✅ NUEVO
                    val imageUrl3 = productResponse.image3.getImageUrl() // ✅ NUEVO

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageUrl,
                        image2 = imageUrl2, // ✅ NUEVO
                        image3 = imageUrl3, // ✅ NUEVO
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

    // ✅ ACTUALIZADO: Ahora acepta imageFile2 e imageFile3
    suspend fun createProductInApi(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        imageFile: File,
        imageFile2: File? = null, // ✅ NUEVO
        imageFile3: File? = null  // ✅ NUEVO
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

            // ✅ NUEVO: Preparar imagen2 si existe
            val imagePart2 = imageFile2?.let { file ->
                if (file.exists()) {
                    val requestFile2 = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image2", file.name, requestFile2)
                } else null
            }

            // ✅ NUEVO: Preparar imagen3 si existe
            val imagePart3 = imageFile3?.let { file ->
                if (file.exists()) {
                    val requestFile3 = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image3", file.name, requestFile3)
                } else null
            }

            val response = apiService.createProduct(
                name = nameBody,
                description = descriptionBody,
                price = priceBody,
                stock = stockBody,
                category = categoryBody,
                image = imagePart,
                image2 = imagePart2, // ✅ NUEVO
                image3 = imagePart3  // ✅ NUEVO
            )

            when {
                response.isSuccessful && response.body() != null -> {
                    val productResponse = response.body()!!
                    val imageUrl = productResponse.image.getImageUrl()
                    val imageUrl2 = productResponse.image2.getImageUrl() // ✅ NUEVO
                    val imageUrl3 = productResponse.image3.getImageUrl() // ✅ NUEVO

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageUrl,
                        image2 = imageUrl2, // ✅ NUEVO
                        image3 = imageUrl3, // ✅ NUEVO
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

    // ✅ ACTUALIZADO: Ahora acepta imageFile2 e imageFile3
    suspend fun updateProductInApi(
        productId: Int,
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        imageFile: File?,
        imageFile2: File? = null, // ✅ NUEVO
        imageFile3: File? = null   // ✅ NUEVO
    ): Result<Product> {
        return try {
            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "UPDATE PRODUCT - INICIO")
            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "Product ID: $productId")
            Log.d(TAG, "Has image1: ${imageFile != null && imageFile.exists()}")
            Log.d(TAG, "Has image2: ${imageFile2 != null && imageFile2.exists()}")
            Log.d(TAG, "Has image3: ${imageFile3 != null && imageFile3.exists()}")

            // ✅ Función auxiliar para subir imagen temporal
            suspend fun uploadTempImage(file: File): com.example.kkarhua.data.remote.ImageUpdateData? {
                val tempNameBody = "TEMP_${System.currentTimeMillis()}".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempDescBody = "temp".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempPriceBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempStockBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempCategoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val tempResponse = apiService.createProduct(
                    name = tempNameBody,
                    description = tempDescBody,
                    price = tempPriceBody,
                    stock = tempStockBody,
                    category = tempCategoryBody,
                    image = imagePart,
                    image2 = null,
                    image3 = null
                )

                if (tempResponse.isSuccessful && tempResponse.body() != null) {
                    val tempProduct = tempResponse.body()!!
                    val tempId = tempProduct.id
                    val imageObject = tempProduct.image

                    try {
                        apiService.deleteProduct(tempId)
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo eliminar producto temporal")
                    }

                    return com.example.kkarhua.data.remote.ImageUpdateData(
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
                }
                return null
            }

            // ✅ Subir imágenes si hay cambios
            val imageData = if (imageFile != null && imageFile.exists()) {
                uploadTempImage(imageFile)
            } else null

            val imageData2 = if (imageFile2 != null && imageFile2.exists()) {
                uploadTempImage(imageFile2)
            } else null

            val imageData3 = if (imageFile3 != null && imageFile3.exists()) {
                uploadTempImage(imageFile3)
            } else null

            val updateData = com.example.kkarhua.data.remote.UpdateProductData(
                name = name,
                description = description,
                price = price,
                stock = stock,
                category = category,
                image = imageData,
                image2 = imageData2, // ✅ NUEVO
                image3 = imageData3  // ✅ NUEVO
            )

            val response = apiService.updateProduct(
                id = productId,
                productData = updateData
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                return Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }

            val productResponse = response.body()
                ?: return Result.failure(Exception("Response body es null"))

            val imageUrl = productResponse.image.getImageUrl()
            val imageUrl2 = productResponse.image2.getImageUrl() // ✅ NUEVO
            val imageUrl3 = productResponse.image3.getImageUrl() // ✅ NUEVO

            val product = Product(
                id = productResponse.id.toString(),
                name = productResponse.name,
                description = productResponse.description,
                price = productResponse.price,
                image = imageUrl,
                image2 = imageUrl2, // ✅ NUEVO
                image3 = imageUrl3, // ✅ NUEVO
                stock = productResponse.stock,
                category = productResponse.category
            )

            insertProduct(product)
            Result.success(product)

        } catch (e: Exception) {
            Log.e(TAG, "EXCEPCIÓN: ${e.message}", e)
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