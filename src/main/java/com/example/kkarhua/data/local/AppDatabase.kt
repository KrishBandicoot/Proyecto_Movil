package com.example.kkarhua.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kkarhua.data.local.CartDao
import com.example.kkarhua.data.local.ProductDao
import com.example.kkarhua.data.local.CartItem
import com.example.kkarhua.data.local.Product

@Database(
    entities = [Product::class, CartItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kkanhua.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}