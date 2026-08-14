package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AccessoryItem
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.MobileItem
import com.example.data.model.RateItem
import com.example.data.model.RepairJob
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleWithItems
import com.example.data.model.ShopProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    // Shop Profile
    @Query("SELECT * FROM shop_profile WHERE id = 1")
    fun getShopProfile(): Flow<ShopProfile?>

    @Query("SELECT * FROM shop_profile WHERE id = 1")
    suspend fun getShopProfileDirect(): ShopProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveShopProfile(profile: ShopProfile)

    // Mobile Stock
    @Query("SELECT * FROM mobile_stock ORDER BY dateAdded DESC")
    fun getAllMobiles(): Flow<List<MobileItem>>

    @Query("SELECT * FROM mobile_stock WHERE id = :id")
    suspend fun getMobileById(id: Long): MobileItem?

    @Query("SELECT * FROM mobile_stock WHERE barcode = :barcode OR imei = :barcode LIMIT 1")
    suspend fun getMobileByBarcode(barcode: String): MobileItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMobile(mobile: MobileItem): Long

    @Update
    suspend fun updateMobile(mobile: MobileItem)

    @Delete
    suspend fun deleteMobile(mobile: MobileItem)

    @Query("UPDATE mobile_stock SET quantity = quantity + :delta WHERE id = :id")
    suspend fun updateMobileQuantity(id: Long, delta: Int)

    // Accessories Stock
    @Query("SELECT * FROM accessories_stock ORDER BY dateAdded DESC")
    fun getAllAccessories(): Flow<List<AccessoryItem>>

    @Query("SELECT * FROM accessories_stock WHERE id = :id")
    suspend fun getAccessoryById(id: Long): AccessoryItem?

    @Query("SELECT * FROM accessories_stock WHERE barcode = :barcode LIMIT 1")
    suspend fun getAccessoryByBarcode(barcode: String): AccessoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessory(accessory: AccessoryItem): Long

    @Update
    suspend fun updateAccessory(accessory: AccessoryItem)

    @Delete
    suspend fun deleteAccessory(accessory: AccessoryItem)

    @Query("UPDATE accessories_stock SET quantity = quantity + :delta WHERE id = :id")
    suspend fun updateAccessoryQuantity(id: Long, delta: Int)

    // Repair Jobs
    @Query("SELECT * FROM repair_jobs ORDER BY dateReceived DESC")
    fun getAllRepairs(): Flow<List<RepairJob>>

    @Query("SELECT * FROM repair_jobs WHERE id = :id")
    suspend fun getRepairById(id: Long): RepairJob?

    @Query("SELECT * FROM repair_jobs WHERE barcode = :code OR jobCode = :code OR imei = :code LIMIT 1")
    suspend fun getRepairByCode(code: String): RepairJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: RepairJob): Long

    @Update
    suspend fun updateRepair(repair: RepairJob)

    @Delete
    suspend fun deleteRepair(repair: RepairJob)

    // Rates
    @Query("SELECT * FROM rate_list ORDER BY title ASC")
    fun getAllRates(): Flow<List<RateItem>>

    @Query("SELECT * FROM rate_list WHERE type = :type ORDER BY title ASC")
    fun getRatesByType(type: String): Flow<List<RateItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: RateItem): Long

    @Update
    suspend fun updateRate(rate: RateItem)

    @Delete
    suspend fun deleteRate(rate: RateItem)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Sales
    @Transaction
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleWithItemsById(id: Long): SaleWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Delete
    suspend fun deleteSale(sale: Sale)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Direct lists for Backup/Export
    @Query("SELECT * FROM mobile_stock")
    suspend fun getMobilesList(): List<MobileItem>

    @Query("SELECT * FROM accessories_stock")
    suspend fun getAccessoriesList(): List<AccessoryItem>

    @Query("SELECT * FROM repair_jobs")
    suspend fun getRepairsList(): List<RepairJob>

    @Query("SELECT * FROM rate_list")
    suspend fun getRatesList(): List<RateItem>

    @Query("SELECT * FROM customers")
    suspend fun getCustomersList(): List<Customer>

    @Query("SELECT * FROM sales")
    suspend fun getSalesList(): List<Sale>

    @Query("SELECT * FROM sale_items")
    suspend fun getSaleItemsList(): List<SaleItem>

    @Query("SELECT * FROM expenses")
    suspend fun getExpensesList(): List<Expense>

    // Bulk insertion for Restore / Demo Seeder
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMobilesBulk(items: List<MobileItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessoriesBulk(items: List<AccessoryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepairsBulk(items: List<RepairJob>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatesBulk(items: List<RateItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomersBulk(items: List<Customer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesBulk(items: List<Sale>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpensesBulk(items: List<Expense>)

    // Clear all for restore
    @Query("DELETE FROM mobile_stock")
    suspend fun clearMobiles()

    @Query("DELETE FROM accessories_stock")
    suspend fun clearAccessories()

    @Query("DELETE FROM repair_jobs")
    suspend fun clearRepairs()

    @Query("DELETE FROM rate_list")
    suspend fun clearRates()

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM sale_items")
    suspend fun clearSaleItems()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()
}
