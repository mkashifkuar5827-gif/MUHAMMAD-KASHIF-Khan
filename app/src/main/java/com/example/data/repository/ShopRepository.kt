package com.example.data.repository

import com.example.data.local.ShopDao
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

class ShopRepository(private val dao: ShopDao) {

    val shopProfile: Flow<ShopProfile?> = dao.getShopProfile()
    val mobiles: Flow<List<MobileItem>> = dao.getAllMobiles()
    val accessories: Flow<List<AccessoryItem>> = dao.getAllAccessories()
    val repairs: Flow<List<RepairJob>> = dao.getAllRepairs()
    val rates: Flow<List<RateItem>> = dao.getAllRates()
    val customers: Flow<List<Customer>> = dao.getAllCustomers()
    val salesWithItems: Flow<List<SaleWithItems>> = dao.getAllSalesWithItems()
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()

    suspend fun getProfileDirect(): ShopProfile {
        return dao.getShopProfileDirect() ?: ShopProfile()
    }

    suspend fun saveProfile(profile: ShopProfile) = dao.saveShopProfile(profile)

    // Mobiles
    suspend fun insertMobile(mobile: MobileItem): Long = dao.insertMobile(mobile)
    suspend fun updateMobile(mobile: MobileItem) = dao.updateMobile(mobile)
    suspend fun deleteMobile(mobile: MobileItem) = dao.deleteMobile(mobile)
    suspend fun updateMobileQuantity(id: Long, delta: Int) = dao.updateMobileQuantity(id, delta)
    suspend fun getMobileByBarcode(barcode: String): MobileItem? = dao.getMobileByBarcode(barcode)
    suspend fun getMobileById(id: Long): MobileItem? = dao.getMobileById(id)

    // Accessories
    suspend fun insertAccessory(accessory: AccessoryItem): Long = dao.insertAccessory(accessory)
    suspend fun updateAccessory(accessory: AccessoryItem) = dao.updateAccessory(accessory)
    suspend fun deleteAccessory(accessory: AccessoryItem) = dao.deleteAccessory(accessory)
    suspend fun updateAccessoryQuantity(id: Long, delta: Int) = dao.updateAccessoryQuantity(id, delta)
    suspend fun getAccessoryByBarcode(barcode: String): AccessoryItem? = dao.getAccessoryByBarcode(barcode)
    suspend fun getAccessoryById(id: Long): AccessoryItem? = dao.getAccessoryById(id)

    // Repairs
    suspend fun insertRepair(repair: RepairJob): Long = dao.insertRepair(repair)
    suspend fun updateRepair(repair: RepairJob) = dao.updateRepair(repair)
    suspend fun deleteRepair(repair: RepairJob) = dao.deleteRepair(repair)
    suspend fun getRepairByCode(code: String): RepairJob? = dao.getRepairByCode(code)
    suspend fun getRepairById(id: Long): RepairJob? = dao.getRepairById(id)

    // Rates
    suspend fun insertRate(rate: RateItem): Long = dao.insertRate(rate)
    suspend fun updateRate(rate: RateItem) = dao.updateRate(rate)
    suspend fun deleteRate(rate: RateItem) = dao.deleteRate(rate)

    // Customers
    suspend fun insertCustomer(customer: Customer): Long = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)
    suspend fun getCustomerById(id: Long): Customer? = dao.getCustomerById(id)

    // Sales
    suspend fun insertSale(sale: Sale, items: List<SaleItem>): Long {
        val saleId = dao.insertSale(sale)
        val linkedItems = items.map { it.copy(saleId = saleId) }
        dao.insertSaleItems(linkedItems)

        // Deduct inventory
        for (item in linkedItems) {
            if (item.itemType == "MOBILE") {
                dao.updateMobileQuantity(item.itemId, -item.quantity)
            } else if (item.itemType == "ACCESSORY") {
                dao.updateAccessoryQuantity(item.itemId, -item.quantity)
            }
        }
        return saleId
    }

    suspend fun getSaleWithItemsById(id: Long): SaleWithItems? = dao.getSaleWithItemsById(id)
    suspend fun deleteSale(sale: Sale) = dao.deleteSale(sale)

    // Expenses
    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    // Backup & Restore
    suspend fun exportAllDataJson(): String {
        val profile = dao.getShopProfileDirect() ?: ShopProfile()
        val mobilesList = dao.getMobilesList()
        val accessoriesList = dao.getAccessoriesList()
        val repairsList = dao.getRepairsList()
        val ratesList = dao.getRatesList()
        val customersList = dao.getCustomersList()
        val salesList = dao.getSalesList()
        val saleItemsList = dao.getSaleItemsList()
        val expensesList = dao.getExpensesList()

        val json = org.json.JSONObject()
        json.put("version", 1)
        json.put("appName", "KASHIF MOBILE AND REPAIR")
        json.put("exportedAt", System.currentTimeMillis())

        // Profile
        val profObj = org.json.JSONObject()
        profObj.put("ownerName", profile.ownerName)
        profObj.put("shopName", profile.shopName)
        profObj.put("phoneNumber", profile.phoneNumber)
        profObj.put("whatsappNumber", profile.whatsappNumber)
        profObj.put("emailAddress", profile.emailAddress)
        profObj.put("shopAddress", profile.shopAddress)
        profObj.put("city", profile.city)
        profObj.put("notes", profile.notes)
        profObj.put("currency", profile.currency)
        profObj.put("language", profile.language)
        profObj.put("invoiceTerms", profile.invoiceTerms)
        json.put("profile", profObj)

        // Mobiles Array
        val mobArray = org.json.JSONArray()
        mobilesList.forEach { m ->
            val obj = org.json.JSONObject()
            obj.put("brand", m.brand)
            obj.put("model", m.model)
            obj.put("ram", m.ram)
            obj.put("storage", m.storage)
            obj.put("color", m.color)
            obj.put("imei", m.imei)
            obj.put("barcode", m.barcode)
            obj.put("purchasePrice", m.purchasePrice)
            obj.put("salePrice", m.salePrice)
            obj.put("quantity", m.quantity)
            obj.put("dateAdded", m.dateAdded)
            obj.put("notes", m.notes)
            mobArray.put(obj)
        }
        json.put("mobiles", mobArray)

        // Accessories Array
        val accArray = org.json.JSONArray()
        accessoriesList.forEach { a ->
            val obj = org.json.JSONObject()
            obj.put("name", a.name)
            obj.put("category", a.category)
            obj.put("barcode", a.barcode)
            obj.put("purchasePrice", a.purchasePrice)
            obj.put("salePrice", a.salePrice)
            obj.put("quantity", a.quantity)
            obj.put("dateAdded", a.dateAdded)
            obj.put("notes", a.notes)
            accArray.put(obj)
        }
        json.put("accessories", accArray)

        // Repairs Array
        val repArray = org.json.JSONArray()
        repairsList.forEach { r ->
            val obj = org.json.JSONObject()
            obj.put("jobCode", r.jobCode)
            obj.put("customerName", r.customerName)
            obj.put("customerPhone", r.customerPhone)
            obj.put("mobileBrand", r.mobileBrand)
            obj.put("mobileModel", r.mobileModel)
            obj.put("imei", r.imei)
            obj.put("barcode", r.barcode)
            obj.put("customerProblem", r.customerProblem)
            obj.put("repairDetails", r.repairDetails)
            obj.put("repairService", r.repairService)
            obj.put("repairCost", r.repairCost)
            obj.put("advancePayment", r.advancePayment)
            obj.put("remainingPayment", r.remainingPayment)
            obj.put("dateReceived", r.dateReceived)
            obj.put("expectedDeliveryDate", r.expectedDeliveryDate)
            obj.put("status", r.status)
            obj.put("notes", r.notes)
            repArray.put(obj)
        }
        json.put("repairs", repArray)

        // Rates Array
        val rateArray = org.json.JSONArray()
        ratesList.forEach { rt ->
            val obj = org.json.JSONObject()
            obj.put("type", rt.type)
            obj.put("title", rt.title)
            obj.put("category", rt.category)
            obj.put("ram", rt.ram)
            obj.put("storage", rt.storage)
            obj.put("color", rt.color)
            obj.put("purchaseRate", rt.purchaseRate)
            obj.put("sellingRate", rt.sellingRate)
            obj.put("standardRate", rt.standardRate)
            obj.put("updatedDate", rt.updatedDate)
            obj.put("notes", rt.notes)
            rateArray.put(obj)
        }
        json.put("rates", rateArray)

        // Customers Array
        val custArray = org.json.JSONArray()
        customersList.forEach { c ->
            val obj = org.json.JSONObject()
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("address", c.address)
            obj.put("notes", c.notes)
            obj.put("createdAt", c.createdAt)
            custArray.put(obj)
        }
        json.put("customers", custArray)

        // Expenses Array
        val expArray = org.json.JSONArray()
        expensesList.forEach { e ->
            val obj = org.json.JSONObject()
            obj.put("name", e.name)
            obj.put("amount", e.amount)
            obj.put("category", e.category)
            obj.put("date", e.date)
            obj.put("description", e.description)
            expArray.put(obj)
        }
        json.put("expenses", expArray)

        return json.toString(2)
    }

    suspend fun restoreDataFromJson(jsonStr: String): Boolean {
        return try {
            val json = org.json.JSONObject(jsonStr)

            if (json.has("profile")) {
                val pObj = json.getJSONObject("profile")
                val profile = ShopProfile(
                    id = 1,
                    ownerName = pObj.optString("ownerName", "Muhammad Kashif"),
                    shopName = pObj.optString("shopName", "KASHIF MOBILE AND REPAIR"),
                    phoneNumber = pObj.optString("phoneNumber", "+966 50 123 4567"),
                    whatsappNumber = pObj.optString("whatsappNumber", "+966 50 123 4567"),
                    emailAddress = pObj.optString("emailAddress", "m.kashifkuar5827@gmail.com"),
                    shopAddress = pObj.optString("shopAddress", "Shop #12, Mobile Market"),
                    city = pObj.optString("city", "Riyadh"),
                    notes = pObj.optString("notes", ""),
                    currency = pObj.optString("currency", "SAR"),
                    language = pObj.optString("language", "en"),
                    invoiceTerms = pObj.optString("invoiceTerms", "")
                )
                dao.saveShopProfile(profile)
            }

            if (json.has("mobiles")) {
                dao.clearMobiles()
                val mobArray = json.getJSONArray("mobiles")
                val mobiles = mutableListOf<MobileItem>()
                for (i in 0 until mobArray.length()) {
                    val obj = mobArray.getJSONObject(i)
                    mobiles.add(
                        MobileItem(
                            brand = obj.optString("brand", ""),
                            model = obj.optString("model", ""),
                            ram = obj.optString("ram", ""),
                            storage = obj.optString("storage", ""),
                            color = obj.optString("color", ""),
                            imei = obj.optString("imei", ""),
                            barcode = obj.optString("barcode", ""),
                            purchasePrice = obj.optDouble("purchasePrice", 0.0),
                            salePrice = obj.optDouble("salePrice", 0.0),
                            quantity = obj.optInt("quantity", 1),
                            dateAdded = obj.optLong("dateAdded", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                dao.insertMobilesBulk(mobiles)
            }

            if (json.has("accessories")) {
                dao.clearAccessories()
                val accArray = json.getJSONArray("accessories")
                val accessories = mutableListOf<AccessoryItem>()
                for (i in 0 until accArray.length()) {
                    val obj = accArray.getJSONObject(i)
                    accessories.add(
                        AccessoryItem(
                            name = obj.optString("name", ""),
                            category = obj.optString("category", "Other Accessories"),
                            barcode = obj.optString("barcode", ""),
                            purchasePrice = obj.optDouble("purchasePrice", 0.0),
                            salePrice = obj.optDouble("salePrice", 0.0),
                            quantity = obj.optInt("quantity", 1),
                            dateAdded = obj.optLong("dateAdded", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                dao.insertAccessoriesBulk(accessories)
            }

            if (json.has("repairs")) {
                dao.clearRepairs()
                val repArray = json.getJSONArray("repairs")
                val repairs = mutableListOf<RepairJob>()
                for (i in 0 until repArray.length()) {
                    val obj = repArray.getJSONObject(i)
                    repairs.add(
                        RepairJob(
                            jobCode = obj.optString("jobCode", ""),
                            customerName = obj.optString("customerName", ""),
                            customerPhone = obj.optString("customerPhone", ""),
                            mobileBrand = obj.optString("mobileBrand", ""),
                            mobileModel = obj.optString("mobileModel", ""),
                            imei = obj.optString("imei", ""),
                            barcode = obj.optString("barcode", ""),
                            customerProblem = obj.optString("customerProblem", ""),
                            repairDetails = obj.optString("repairDetails", ""),
                            repairService = obj.optString("repairService", ""),
                            repairCost = obj.optDouble("repairCost", 0.0),
                            advancePayment = obj.optDouble("advancePayment", 0.0),
                            remainingPayment = obj.optDouble("remainingPayment", 0.0),
                            dateReceived = obj.optLong("dateReceived", System.currentTimeMillis()),
                            expectedDeliveryDate = obj.optLong("expectedDeliveryDate", System.currentTimeMillis()),
                            status = obj.optString("status", "PENDING"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                dao.insertRepairsBulk(repairs)
            }

            if (json.has("rates")) {
                dao.clearRates()
                val rateArray = json.getJSONArray("rates")
                val rates = mutableListOf<RateItem>()
                for (i in 0 until rateArray.length()) {
                    val obj = rateArray.getJSONObject(i)
                    rates.add(
                        RateItem(
                            type = obj.optString("type", "MOBILE"),
                            title = obj.optString("title", ""),
                            category = obj.optString("category", ""),
                            ram = obj.optString("ram", ""),
                            storage = obj.optString("storage", ""),
                            color = obj.optString("color", ""),
                            purchaseRate = obj.optDouble("purchaseRate", 0.0),
                            sellingRate = obj.optDouble("sellingRate", 0.0),
                            standardRate = obj.optDouble("standardRate", obj.optDouble("sellingRate", 0.0)),
                            updatedDate = obj.optLong("updatedDate", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                dao.insertRatesBulk(rates)
            }

            if (json.has("customers")) {
                dao.clearCustomers()
                val custArray = json.getJSONArray("customers")
                val customers = mutableListOf<Customer>()
                for (i in 0 until custArray.length()) {
                    val obj = custArray.getJSONObject(i)
                    customers.add(
                        Customer(
                            name = obj.optString("name", ""),
                            phone = obj.optString("phone", ""),
                            address = obj.optString("address", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertCustomersBulk(customers)
            }

            if (json.has("expenses")) {
                dao.clearExpenses()
                val expArray = json.getJSONArray("expenses")
                val expenses = mutableListOf<Expense>()
                for (i in 0 until expArray.length()) {
                    val obj = expArray.getJSONObject(i)
                    expenses.add(
                        Expense(
                            name = obj.optString("name", ""),
                            amount = obj.optDouble("amount", 0.0),
                            category = obj.optString("category", "Other"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            description = obj.optString("description", "")
                        )
                    )
                }
                dao.insertExpensesBulk(expenses)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
