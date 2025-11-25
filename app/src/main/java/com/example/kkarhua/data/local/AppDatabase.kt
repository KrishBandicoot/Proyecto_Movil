package com.example.kkarhua.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Product::class, CartItem::class],
    version = 5, // ✅ Incrementamos a versión 5
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE products ADD COLUMN stock INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
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

                database.execSQL("""
                    INSERT INTO products_new (id, name, description, price, image, stock)
                    SELECT id, name, description, price, imageUrl, stock
                    FROM products
                """.trimIndent())

                database.execSQL("DROP TABLE products")
                database.execSQL("ALTER TABLE products_new RENAME TO products")

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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE products ADD COLUMN category TEXT NOT NULL DEFAULT ''")
            }
        }

        // ✅ NUEVA MIGRACIÓN: Agregar image2 e image3
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE products ADD COLUMN image2 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE products ADD COLUMN image3 TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kkanhua.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}