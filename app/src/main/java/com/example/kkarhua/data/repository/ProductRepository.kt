package com.example.kkarhua.data.repository

import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.local.ProductDao
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

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

    // Datos de muestra para inicializar la BD
    suspend fun insertSampleData() {
        val sampleProducts = listOf(
            Product(
                id = "1",
                name = "Collar de Plata",
                description = "Elegante collar de plata con diseño minimalista",
                price = 45.99,
                imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=400",
                category = "Collares",
                inStock = true
            ),
            Product(
                id = "2",
                name = "Aretes de Oro",
                description = "Aretes de oro 18k con piedras preciosas",
                price = 89.99,
                imageUrl = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=400",
                category = "Aretes",
                inStock = true
            ),
            Product(
                id = "3",
                name = "Pulsera Artesanal",
                description = "Pulsera hecha a mano con hilos de colores",
                price = 15.99,
                imageUrl = "https://images.unsplash.com/photo-1611591437281-460bfbe1220a?w=400",
                category = "Pulseras",
                inStock = true
            ),
            Product(
                id = "4",
                name = "Anillo de Compromiso",
                description = "Hermoso anillo con diamante central",
                price = 299.99,
                imageUrl = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=400",
                category = "Anillos",
                inStock = true
            ),
            Product(
                id = "5",
                name = "Reloj Clásico",
                description = "Reloj de pulsera con correa de cuero",
                price = 125.50,
                imageUrl = "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=400",
                category = "Relojes",
                inStock = true
            ),
            Product(
                id = "6",
                name = "Broche Vintage",
                description = "Broche antiguo restaurado con detalles dorados",
                price = 34.99,
                imageUrl = "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=400",
                category = "Broches",
                inStock = true
            )
        )
        insertAllProducts(sampleProducts)
    }
}