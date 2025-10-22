package com.example.kkarhua.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Product::class, CartItem::class],
    version = 3, // Incrementamos la versión a 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de versión 1 a 2 para agregar el campo stock
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna stock a la tabla products
                database.execSQL("ALTER TABLE products ADD COLUMN stock INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migración de versión 2 a 3 para renombrar imageUrl a image
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal con la nueva estructura
                database.execSQL("""
                    CREATE TABLE products_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        price REAL NOT NULL,
                        image TEXT NOT NULL,
                        stock INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Copiar datos de la tabla antigua a la nueva
                database.execSQL("""
                    INSERT INTO products_new (id, name, description, price, image, stock)
                    SELECT id, name, description, price, imageUrl, stock
                    FROM products
                """.trimIndent())

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE products")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE products_new RENAME TO products")

                // Hacer lo mismo con cart_items
                database.execSQL("""
                    CREATE TABLE cart_items_new (
                        productId TEXT PRIMARY KEY NOT NULL,
                        productName TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL,
                        image TEXT NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO cart_items_new (productId, productName, quantity, price, image)
                    SELECT productId, productName, quantity, price, imageUrl
                    FROM cart_items
                """.trimIndent())

                database.execSQL("DROP TABLE cart_items")
                database.execSQL("ALTER TABLE cart_items_new RENAME TO cart_items")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kkanhua.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}