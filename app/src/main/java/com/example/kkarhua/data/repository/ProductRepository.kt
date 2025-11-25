package com.example.kkarhua.data.repository

import android.util.Base64
import android.util.Log
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.local.ProductDao
import com.example.kkarhua.data.remote.RetrofitClient
import com.example.kkarhua.data.remote.getImageUrl
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductRepository(private val productDao: ProductDao) {

    private val apiService = RetrofitClient.apiService
    private val TAG = "ProductRepository"

    // String Base64 de una imagen JPEG blanca de 1x1 pixel (Para el hack de Edit)
    private val TINY_IMAGE_BASE64 = "/9j/4AAQSkZJRgABAQEAYABgAAD/4QAiRXhpZgAATU0AKgAAAAgAAQESAAMAAAABAAEAAAAAAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCAABAAEGMQD/2gAMAwEAAhEDEQA/AH8AP/Z"

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
                            val imageUrl2 = productResponse.image2.getImageUrl()
                            val imageUrl3 = productResponse.image3.getImageUrl()

                            Product(
                                id = productResponse.id.toString(),
                                name = productResponse.name,
                                description = productResponse.description,
                                price = productResponse.price,
                                image = imageUrl,
                                image2 = imageUrl2,
                                image3 = imageUrl3,
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
                    val imageUrl2 = productResponse.image2.getImageUrl()
                    val imageUrl3 = productResponse.image3.getImageUrl()

                    val product = Product(
                        id = productResponse.id.toString(),
                        name = productResponse.name,
                        description = productResponse.description,
                        price = productResponse.price,
                        image = imageUrl,
                        image2 = imageUrl2,
                        image3 = imageUrl3,
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

    // Helper para detectar si es PNG o JPG
    private fun getFileMediaType(file: File): MediaType? {
        return if (file.extension.equals("png", ignoreCase = true)) {
            "image/png".toMediaTypeOrNull()
        } else {
            "image/jpeg".toMediaTypeOrNull()
        }
    }

    // ============================================================================================
    // 1. CREATE PRODUCT (ADD PRODUCT) - DEBUGGING ACTIVADO
    // ============================================================================================
    suspend fun createProductInApi(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        imageFile: File,
        imageFile2: File? = null,
        imageFile3: File? = null
    ): Result<Product> {
        return try {
            Log.d(TAG, "════════ DEBUG ADD PRODUCT ════════")
            Log.d(TAG, "Parametro Img1: ${imageFile.name} | Size: ${imageFile.length()}")
            Log.d(TAG, "Parametro Img2: ${imageFile2?.name} | Size: ${imageFile2?.length()}")
            Log.d(TAG, "Parametro Img3: ${imageFile3?.name} | Size: ${imageFile3?.length()}")

            // 1. VALIDACIONES
            if (!imageFile.exists() || imageFile.length() == 0L) return Result.failure(Exception("Img1 vacía"))
            if (imageFile2 == null || !imageFile2.exists()) return Result.failure(Exception("Img2 obligatoria"))
            if (imageFile3 == null || !imageFile3.exists()) return Result.failure(Exception("Img3 obligatoria"))

            // 2. TEXT BODIES
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

            // 3. PREPARACIÓN DE IMÁGENES (VARIABLES ÚNICAS PARA EVITAR ERRORES)

            // --- IMAGEN 1 ---
            val mime1 = getFileMediaType(imageFile)
            val reqBodyImg1 = imageFile.asRequestBody(mime1)
            val partImg1 = MultipartBody.Part.createFormData("image", imageFile.name, reqBodyImg1)
            Log.d(TAG, ">> Part 1 creada con: ${imageFile.name} ($mime1)")

            // --- IMAGEN 2 ---
            val mime2 = getFileMediaType(imageFile2)
            val reqBodyImg2 = imageFile2.asRequestBody(mime2)
            val partImg2 = MultipartBody.Part.createFormData("image2", imageFile2.name, reqBodyImg2)
            Log.d(TAG, ">> Part 2 creada con: ${imageFile2.name} ($mime2)")

            // --- IMAGEN 3 ---
            val mime3 = getFileMediaType(imageFile3)
            val reqBodyImg3 = imageFile3.asRequestBody(mime3)
            val partImg3 = MultipartBody.Part.createFormData("image3", imageFile3.name, reqBodyImg3)
            Log.d(TAG, ">> Part 3 creada con: ${imageFile3.name} ($mime3)")

            // 4. LLAMADA API
            val response = apiService.createProduct(
                name = nameBody,
                description = descriptionBody,
                price = priceBody,
                stock = stockBody,
                category = categoryBody,
                image = partImg1,  // Única variable partImg1
                image2 = partImg2, // Única variable partImg2
                image3 = partImg3  // Única variable partImg3
            )

            when {
                response.isSuccessful && response.body() != null -> {
                    val prod = response.body()!!
                    Log.d(TAG, "✓ Éxito. URLs recibidas:")
                    Log.d(TAG, "  1: ${prod.image.getImageUrl()}")
                    Log.d(TAG, "  2: ${prod.image2.getImageUrl()}")
                    Log.d(TAG, "  3: ${prod.image3.getImageUrl()}")

                    val product = Product(
                        id = prod.id.toString(),
                        name = prod.name,
                        description = prod.description,
                        price = prod.price,
                        image = prod.image.getImageUrl(),
                        image2 = prod.image2.getImageUrl(),
                        image3 = prod.image3.getImageUrl(),
                        stock = prod.stock,
                        category = prod.category
                    )
                    insertProduct(product)
                    Result.success(product)
                }
                else -> {
                    val err = response.errorBody()?.string()
                    Log.e(TAG, "Error API: $err")
                    Result.failure(Exception("Error ${response.code()}: $err"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================================================
    // 2. UPDATE PRODUCT (EDIT PRODUCT)
    // ============================================================================================
    suspend fun updateProductInApi(
        productId: Int,
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        imageFile: File?,
        imageFile2: File? = null,
        imageFile3: File? = null
    ): Result<Product> {
        return try {
            Log.d(TAG, "UPDATE PRODUCT - INICIO ID: $productId")

            // Helper temporal para editar (repite la imagen para pasar validación de Xano)
            suspend fun uploadTempImage(file: File): com.example.kkarhua.data.remote.ImageUpdateData? {
                val tempName = "TEMP_${System.currentTimeMillis()}".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempDesc = "temp".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempPrice = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempStock = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tempCat = category.toRequestBody("text/plain".toMediaTypeOrNull())

                val mediaType = getFileMediaType(file)
                val reqFile = file.asRequestBody(mediaType)

                // AQUÍ SÍ repetimos intencionalmente para el HACK de Edición
                val p1 = MultipartBody.Part.createFormData("image", file.name, reqFile)
                val p2 = MultipartBody.Part.createFormData("image2", file.name, reqFile)
                val p3 = MultipartBody.Part.createFormData("image3", file.name, reqFile)

                val tempRes = apiService.createProduct(
                    name = tempName, description = tempDesc, price = tempPrice,
                    stock = tempStock, category = tempCat,
                    image = p1, image2 = p2, image3 = p3
                )

                if (tempRes.isSuccessful && tempRes.body() != null) {
                    val tProd = tempRes.body()!!
                    val tId = tProd.id
                    val imgObj = tProd.image

                    if (imgObj == null || imgObj.url.isNullOrEmpty()) {
                        try { apiService.deleteProduct(tId) } catch (e: Exception) {}
                        return null
                    }

                    try { apiService.deleteProduct(tId) } catch (e: Exception) {}

                    return com.example.kkarhua.data.remote.ImageUpdateData(
                        path = imgObj.path ?: "",
                        name = imgObj.name ?: "",
                        type = imgObj.type ?: "",
                        size = imgObj.size ?: 0,
                        mime = imgObj.mime ?: "",
                        url = imgObj.url ?: "",
                        meta = com.example.kkarhua.data.remote.ImageMetaUpdate(
                            width = imgObj.meta?.width ?: 0,
                            height = imgObj.meta?.height ?: 0
                        )
                    )
                }
                return null
            }

            val imgD1 = if (imageFile != null && imageFile.exists()) uploadTempImage(imageFile) else null
            val imgD2 = if (imageFile2 != null && imageFile2.exists()) uploadTempImage(imageFile2) else null
            val imgD3 = if (imageFile3 != null && imageFile3.exists()) uploadTempImage(imageFile3) else null

            val updateData = com.example.kkarhua.data.remote.UpdateProductData(
                name = name, description = description, price = price,
                stock = stock, category = category,
                image = imgD1, image2 = imgD2, image3 = imgD3
            )

            val response = apiService.updateProduct(id = productId, productData = updateData)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                return Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }

            val prodRes = response.body() ?: return Result.failure(Exception("Response null"))

            val prod = Product(
                id = prodRes.id.toString(),
                name = prodRes.name,
                description = prodRes.description,
                price = prodRes.price,
                image = prodRes.image.getImageUrl(),
                image2 = prodRes.image2.getImageUrl(),
                image3 = prodRes.image3.getImageUrl(),
                stock = prodRes.stock,
                category = prodRes.category
            )
            insertProduct(prod)
            Result.success(prod)

        } catch (e: Exception) {
            Log.e(TAG, "EXCEPCIÓN UPDATE: ${e.message}", e)
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