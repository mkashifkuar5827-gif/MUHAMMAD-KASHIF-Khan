package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
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
import com.example.data.repository.SampleDataSeeder
import com.example.data.repository.ShopRepository
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageStateManager
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.currentPaletteState
import com.example.ui.theme.getThemePalette
import com.example.util.BarcodeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CartItem(
    val itemType: String, // "MOBILE", "ACCESSORY"
    val itemId: Long,
    val itemName: String,
    val barcode: String,
    val unitPrice: Double,
    val purchasePrice: Double,
    var quantity: Int,
    val maxAvailable: Int
) {
    val totalPrice: Double get() = unitPrice * quantity
}

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository

    val profileState: StateFlow<ShopProfile>
    val mobilesState: StateFlow<List<MobileItem>>
    val accessoriesState: StateFlow<List<AccessoryItem>>
    val repairsState: StateFlow<List<RepairJob>>
    val ratesState: StateFlow<List<RateItem>>
    val customersState: StateFlow<List<Customer>>
    val salesState: StateFlow<List<SaleWithItems>>
    val expensesState: StateFlow<List<Expense>>

    // Cart / POS State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomerName = MutableStateFlow("Cash Customer")
    val selectedCustomerName: StateFlow<String> = _selectedCustomerName.asStateFlow()

    private val _selectedCustomerPhone = MutableStateFlow("")
    val selectedCustomerPhone: StateFlow<String> = _selectedCustomerPhone.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount.asStateFlow()

    // Active message / snackbar
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Last completed sale for instant receipt dialog
    private val _lastCompletedSale = MutableStateFlow<SaleWithItems?>(null)
    val lastCompletedSale: StateFlow<SaleWithItems?> = _lastCompletedSale.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.shopDao()
        repository = ShopRepository(dao)

        profileState = repository.shopProfile
            .combine(MutableStateFlow(Unit)) { prof, _ -> prof ?: ShopProfile() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ShopProfile())

        mobilesState = repository.mobiles
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        accessoriesState = repository.accessories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        repairsState = repository.repairs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        ratesState = repository.rates
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        customersState = repository.customers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        salesState = repository.salesWithItems
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        expensesState = repository.expenses
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Ensure default profile and seed if fresh
        viewModelScope.launch {
            val prof = dao.getShopProfileDirect()
            if (prof == null) {
                SampleDataSeeder.seedSampleData(dao)
            } else {
                currentPaletteState.value = getThemePalette(AppThemeMode.fromId(prof.theme))
                LanguageStateManager.initialize(prof.language)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun clearLastCompletedSale() {
        _lastCompletedSale.value = null
    }

    // Language & Profile Settings
    fun setLanguage(lang: AppLanguage) {
        LanguageStateManager.setLanguage(lang)
        viewModelScope.launch {
            val cur = profileState.value
            repository.saveProfile(cur.copy(language = lang.code))
            _message.value = "Language set to ${lang.displayName} (${lang.nativeName})"
        }
    }

    fun toggleLanguage() {
        val nextLang = LanguageStateManager.toggleNextLanguage()
        viewModelScope.launch {
            val cur = profileState.value
            repository.saveProfile(cur.copy(language = nextLang.code))
            _message.value = "Language changed to ${nextLang.displayName} (${nextLang.nativeName})"
        }
    }

    fun setTheme(theme: AppThemeMode) {
        currentPaletteState.value = getThemePalette(theme)
        viewModelScope.launch {
            val cur = profileState.value
            repository.saveProfile(cur.copy(theme = theme.id))
            _message.value = "Theme changed to ${theme.title}"
        }
    }

    fun updateProfile(profile: ShopProfile) {
        currentPaletteState.value = getThemePalette(AppThemeMode.fromId(profile.theme))
        LanguageStateManager.setLanguageByCode(profile.language)
        viewModelScope.launch {
            repository.saveProfile(profile)
            _message.value = "Shop profile saved successfully"
        }
    }

    // Mobiles CRUD
    fun saveMobile(mobile: MobileItem) {
        viewModelScope.launch {
            val barcode = if (mobile.barcode.isBlank()) BarcodeUtils.generateRandomBarcode("KMR-MOB") else mobile.barcode
            val itemToSave = mobile.copy(barcode = barcode)
            if (mobile.id == 0L) {
                repository.insertMobile(itemToSave)
                _message.value = "Mobile ${mobile.brand} ${mobile.model} added"
            } else {
                repository.updateMobile(itemToSave)
                _message.value = "Mobile updated successfully"
            }
        }
    }

    fun deleteMobile(mobile: MobileItem) {
        viewModelScope.launch {
            repository.deleteMobile(mobile)
            _message.value = "Mobile deleted"
        }
    }

    fun adjustMobileStock(mobile: MobileItem, delta: Int) {
        viewModelScope.launch {
            val newQty = (mobile.quantity + delta).coerceAtLeast(0)
            repository.updateMobile(mobile.copy(quantity = newQty))
        }
    }

    // Accessories CRUD
    fun saveAccessory(accessory: AccessoryItem) {
        viewModelScope.launch {
            val barcode = if (accessory.barcode.isBlank()) BarcodeUtils.generateRandomBarcode("KMR-ACC") else accessory.barcode
            val itemToSave = accessory.copy(barcode = barcode)
            if (accessory.id == 0L) {
                repository.insertAccessory(itemToSave)
                _message.value = "Accessory ${accessory.name} added"
            } else {
                repository.updateAccessory(itemToSave)
                _message.value = "Accessory updated successfully"
            }
        }
    }

    fun deleteAccessory(accessory: AccessoryItem) {
        viewModelScope.launch {
            repository.deleteAccessory(accessory)
            _message.value = "Accessory deleted"
        }
    }

    fun adjustAccessoryStock(accessory: AccessoryItem, delta: Int) {
        viewModelScope.launch {
            val newQty = (accessory.quantity + delta).coerceAtLeast(0)
            repository.updateAccessory(accessory.copy(quantity = newQty))
        }
    }

    // Repairs CRUD
    fun saveRepair(repair: RepairJob) {
        viewModelScope.launch {
            val jobCode = if (repair.jobCode.isBlank()) BarcodeUtils.generateJobCode() else repair.jobCode
            val barcode = if (repair.barcode.isBlank()) jobCode else repair.barcode
            val remaining = (repair.repairCost - repair.advancePayment).coerceAtLeast(0.0)
            val toSave = repair.copy(jobCode = jobCode, barcode = barcode, remainingPayment = remaining)

            if (repair.id == 0L) {
                repository.insertRepair(toSave)
                _message.value = "Repair token $jobCode created"
            } else {
                repository.updateRepair(toSave)
                _message.value = "Repair job updated"
            }
        }
    }

    fun updateRepairStatus(repair: RepairJob, newStatus: String) {
        viewModelScope.launch {
            val isDelivered = newStatus.equals("DELIVERED", ignoreCase = true)
            val remaining = if (isDelivered) 0.0 else repair.remainingPayment
            val advance = if (isDelivered) repair.repairCost else repair.advancePayment
            repository.updateRepair(repair.copy(status = newStatus, advancePayment = advance, remainingPayment = remaining))
            _message.value = "Repair status updated to $newStatus"
        }
    }

    fun receiveRepairBalance(repair: RepairJob, paymentReceived: Double) {
        viewModelScope.launch {
            val newAdvance = repair.advancePayment + paymentReceived
            val newRemaining = (repair.repairCost - newAdvance).coerceAtLeast(0.0)
            val newStatus = if (newRemaining <= 0) "COMPLETED" else repair.status
            repository.updateRepair(repair.copy(advancePayment = newAdvance, remainingPayment = newRemaining, status = newStatus))
            _message.value = "Payment of ${String.format("%.2f", paymentReceived)} received for ${repair.jobCode}"
        }
    }

    fun deleteRepair(repair: RepairJob) {
        viewModelScope.launch {
            repository.deleteRepair(repair)
            _message.value = "Repair record deleted"
        }
    }

    // Rates CRUD
    fun saveRate(rate: RateItem) {
        viewModelScope.launch {
            val toSave = rate.copy(updatedDate = System.currentTimeMillis())
            if (rate.id == 0L) {
                repository.insertRate(toSave)
                _message.value = "Rate added"
            } else {
                repository.updateRate(toSave)
                _message.value = "Rate updated"
            }
        }
    }

    fun deleteRate(rate: RateItem) {
        viewModelScope.launch {
            repository.deleteRate(rate)
            _message.value = "Rate removed"
        }
    }

    // Customers CRUD
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
                _message.value = "Customer ${customer.name} added"
            } else {
                repository.updateCustomer(customer)
                _message.value = "Customer updated"
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _message.value = "Customer deleted"
        }
    }

    // Expenses CRUD
    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            if (expense.id == 0L) {
                repository.insertExpense(expense)
                _message.value = "Expense ${expense.name} recorded"
            } else {
                repository.updateExpense(expense)
                _message.value = "Expense updated"
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _message.value = "Expense deleted"
        }
    }

    // POS / Sales Management
    fun setCustomerDetails(name: String, phone: String) {
        _selectedCustomerName.value = name
        _selectedCustomerPhone.value = phone
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = amount
    }

    fun setPaidAmount(amount: Double) {
        _paidAmount.value = amount
    }

    fun addMobileToCart(mobile: MobileItem) {
        if (mobile.quantity <= 0) {
            _message.value = "Mobile is out of stock!"
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.itemType == "MOBILE" && it.itemId == mobile.id }
        if (index >= 0) {
            val item = current[index]
            if (item.quantity < mobile.quantity) {
                current[index] = item.copy(quantity = item.quantity + 1)
                _cartItems.value = current
            } else {
                _message.value = "Cannot exceed available stock (${mobile.quantity})"
            }
        } else {
            current.add(
                CartItem(
                    itemType = "MOBILE",
                    itemId = mobile.id,
                    itemName = "${mobile.brand} ${mobile.model} (${mobile.storage} ${mobile.color})",
                    barcode = mobile.barcode.ifBlank { mobile.imei },
                    unitPrice = mobile.salePrice,
                    purchasePrice = mobile.purchasePrice,
                    quantity = 1,
                    maxAvailable = mobile.quantity
                )
            )
            _cartItems.value = current
        }
    }

    fun addAccessoryToCart(accessory: AccessoryItem) {
        if (accessory.quantity <= 0) {
            _message.value = "Accessory is out of stock!"
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.itemType == "ACCESSORY" && it.itemId == accessory.id }
        if (index >= 0) {
            val item = current[index]
            if (item.quantity < accessory.quantity) {
                current[index] = item.copy(quantity = item.quantity + 1)
                _cartItems.value = current
            } else {
                _message.value = "Cannot exceed available stock (${accessory.quantity})"
            }
        } else {
            current.add(
                CartItem(
                    itemType = "ACCESSORY",
                    itemId = accessory.id,
                    itemName = accessory.name,
                    barcode = accessory.barcode,
                    unitPrice = accessory.salePrice,
                    purchasePrice = accessory.purchasePrice,
                    quantity = 1,
                    maxAvailable = accessory.quantity
                )
            )
            _cartItems.value = current
        }
    }

    fun updateCartItemQuantity(index: Int, qty: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            if (qty <= 0) {
                current.removeAt(index)
            } else if (qty <= item.maxAvailable) {
                current[index] = item.copy(quantity = qty)
            } else {
                _message.value = "Cannot exceed available stock (${item.maxAvailable})"
            }
            _cartItems.value = current
        }
    }

    fun removeCartItem(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountAmount.value = 0.0
        _paidAmount.value = 0.0
        _selectedCustomerName.value = "Cash Customer"
        _selectedCustomerPhone.value = ""
    }

    fun completeSale(paid: Double) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _message.value = "Cart is empty!"
            return
        }

        viewModelScope.launch {
            val subtotal = items.sumOf { it.totalPrice }
            val discount = _discountAmount.value
            val grandTotal = (subtotal - discount).coerceAtLeast(0.0)
            val remaining = (grandTotal - paid).coerceAtLeast(0.0)
            val invoiceNumber = BarcodeUtils.generateInvoiceNumber()

            val sale = Sale(
                invoiceNumber = invoiceNumber,
                customerName = _selectedCustomerName.value.ifBlank { "Cash Customer" },
                customerPhone = _selectedCustomerPhone.value,
                date = System.currentTimeMillis(),
                subtotal = subtotal,
                discount = discount,
                grandTotal = grandTotal,
                paidAmount = paid,
                remainingAmount = remaining
            )

            val saleItems = items.map { item ->
                SaleItem(
                    saleId = 0,
                    itemType = item.itemType,
                    itemId = item.itemId,
                    itemName = item.itemName,
                    barcode = item.barcode,
                    unitPrice = item.unitPrice,
                    purchasePrice = item.purchasePrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice
                )
            }

            val saleId = repository.insertSale(sale, saleItems)
            val savedWithItems = repository.getSaleWithItemsById(saleId)
            _lastCompletedSale.value = savedWithItems

            // Save customer if new
            val custName = _selectedCustomerName.value
            if (custName.isNotBlank() && custName != "Cash Customer") {
                val existing = customersState.value.find { it.name.equals(custName, true) || (it.phone.isNotBlank() && it.phone == _selectedCustomerPhone.value) }
                if (existing == null) {
                    repository.insertCustomer(Customer(name = custName, phone = _selectedCustomerPhone.value))
                }
            }

            clearCart()
            _message.value = "Sale completed! Invoice #$invoiceNumber created."
        }
    }

    fun deleteInvoice(saleWithItems: SaleWithItems) {
        viewModelScope.launch {
            repository.deleteSale(saleWithItems.sale)
            _message.value = "Invoice deleted"
        }
    }

    // Barcode scan product lookup helper
    suspend fun findProductByBarcode(code: String): Any? {
        val trimmed = code.trim()
        val mobile = repository.getMobileByBarcode(trimmed)
        if (mobile != null) return mobile

        val accessory = repository.getAccessoryByBarcode(trimmed)
        if (accessory != null) return accessory

        val repair = repository.getRepairByCode(trimmed)
        if (repair != null) return repair

        return null
    }

    // Backup & Restore
    suspend fun exportBackupJson(): String {
        return repository.exportAllDataJson()
    }

    suspend fun restoreBackupJson(json: String): Boolean {
        val success = repository.restoreDataFromJson(json)
        if (success) {
            _message.value = "Database restored successfully"
        } else {
            _message.value = "Failed to restore backup. Invalid JSON format."
        }
        return success
    }

    fun loadSampleDemoData() {
        viewModelScope.launch {
            val database = AppDatabase.getDatabase(getApplication())
            SampleDataSeeder.seedSampleData(database.shopDao())
            _message.value = "Sample mobile shop demo data loaded!"
        }
    }
}
