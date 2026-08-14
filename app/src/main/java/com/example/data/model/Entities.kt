package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val ownerName: String = "Muhammad Kashif",
    val shopName: String = "KASHIF MOBILE AND REPAIR",
    val phoneNumber: String = "+966 50 123 4567",
    val whatsappNumber: String = "+966 50 123 4567",
    val emailAddress: String = "m.kashifkuar5827@gmail.com",
    val shopAddress: String = "Shop #12, Mobile Market, King Fahd Road",
    val city: String = "Riyadh",
    val notes: String = "Specialist in All Mobile Sales, Hardware & Software Repairs, Original Accessories",
    val currency: String = "SAR",
    val language: String = "en",
    val theme: String = "GOLD",
    val invoiceTerms: String = "Checked warranty only. Please bring receipt for repair claim within 7 days."
)

@Entity(
    tableName = "mobile_stock",
    indices = [Index(value = ["barcode"]), Index(value = ["imei"])]
)
data class MobileItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val model: String,
    val ram: String = "",
    val storage: String = "",
    val color: String = "",
    val imei: String = "",
    val barcode: String = "",
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val quantity: Int = 1,
    val dateAdded: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "accessories_stock",
    indices = [Index(value = ["barcode"])]
)
data class AccessoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Other Accessories",
    val barcode: String = "",
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val quantity: Int = 1,
    val dateAdded: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "repair_jobs",
    indices = [Index(value = ["barcode"]), Index(value = ["jobCode"]), Index(value = ["status"])]
)
data class RepairJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobCode: String = "",
    val customerName: String,
    val customerPhone: String = "",
    val mobileBrand: String = "",
    val mobileModel: String = "",
    val imei: String = "",
    val barcode: String = "",
    val customerProblem: String = "",
    val repairDetails: String = "",
    val repairService: String = "",
    val repairCost: Double = 0.0,
    val advancePayment: Double = 0.0,
    val remainingPayment: Double = 0.0,
    val dateReceived: Long = System.currentTimeMillis(),
    val expectedDeliveryDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, REPAIRING, COMPLETED, DELIVERED, CANCELLED
    val notes: String = ""
)

@Entity(tableName = "rate_list")
data class RateItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "MOBILE", "ACCESSORY", "REPAIR_SERVICE"
    val title: String,
    val category: String = "",
    val ram: String = "",
    val storage: String = "",
    val color: String = "",
    val purchaseRate: Double = 0.0,
    val sellingRate: Double = 0.0,
    val standardRate: Double = 0.0,
    val updatedDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    indices = [Index(value = ["invoiceNumber"], unique = true)]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Cash Customer",
    val customerPhone: String = "",
    val date: Long = System.currentTimeMillis(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val itemType: String, // "MOBILE", "ACCESSORY", "REPAIR", "OTHER"
    val itemId: Long = 0,
    val itemName: String,
    val barcode: String = "",
    val unitPrice: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val quantity: Int = 1,
    val totalPrice: Double = 0.0
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String = "Other",
    val date: Long = System.currentTimeMillis(),
    val description: String = ""
)

typealias ExpenseItem = Expense

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItem>
)
