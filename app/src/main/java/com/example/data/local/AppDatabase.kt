package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AccessoryItem
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.MobileItem
import com.example.data.model.RateItem
import com.example.data.model.RepairJob
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.ShopProfile

@Database(
    entities = [
        ShopProfile::class,
        MobileItem::class,
        AccessoryItem::class,
        RepairJob::class,
        RateItem::class,
        Customer::class,
        Sale::class,
        SaleItem::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kashif_mobile_shop.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
