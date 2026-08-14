package com.example.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccessoryItem
import com.example.data.model.Customer
import com.example.data.model.MobileItem
import com.example.data.model.SaleWithItems
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.locale.stringRes
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CartItem
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesPosScreen(viewModel: ShopViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: POS Register, 1: Sales History

    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val mobiles by viewModel.mobilesState.collectAsStateWithLifecycle()
    val accessories by viewModel.accessoriesState.collectAsStateWithLifecycle()
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val salesHistory by viewModel.salesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsStateWithLifecycle()

    val currency = profile.currency

    var showScannerForCart by remember { mutableStateOf(false) }
    var selectedReceiptSale by remember { mutableStateOf<SaleWithItems?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleWithItems?>(null) }

    var showCartDialog by remember { mutableStateOf(false) }

    // Last completed sale auto popup
    if (lastCompletedSale != null) {
        InvoiceReceiptDialog(
            saleWithItems = lastCompletedSale!!,
            profile = profile,
            onDismiss = { viewModel.clearLastCompletedSale() }
        )
    }

    if (selectedReceiptSale != null) {
        InvoiceReceiptDialog(
            saleWithItems = selectedReceiptSale!!,
            profile = profile,
            onDismiss = { selectedReceiptSale = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringRes("pos_terminal"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringRes("sales_history"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // POS REGISTER TAB
            PosRegisterContent(
                viewModel = viewModel,
                cartItems = cartItems,
                mobiles = mobiles,
                accessories = accessories,
                customers = customers,
                currency = currency,
                onOpenScanner = { showScannerForCart = true },
                onOpenCartModal = { showCartDialog = true }
            )
        } else {
            // SALES HISTORY / INVOICES TAB
            SalesHistoryContent(
                salesHistory = salesHistory,
                currency = currency,
                onViewReceipt = { selectedReceiptSale = it },
                onDeleteSale = { saleToDelete = it }
            )
        }
    }

    // Camera Barcode Scanner -> Auto Add to Cart
    if (showScannerForCart) {
        BarcodeScannerModal(
            onDismiss = { showScannerForCart = false },
            onBarcodeScanned = { scannedCode ->
                showScannerForCart = false
                coroutineScope.launch {
                    val item = viewModel.findProductByBarcode(scannedCode)
                    if (item is MobileItem) {
                        viewModel.addMobileToCart(item)
                    } else if (item is AccessoryItem) {
                        viewModel.addAccessoryToCart(item)
                    }
                }
            }
        )
    }

    if (saleToDelete != null) {
        ConfirmationDialog(
            title = "Delete Invoice",
            message = "Are you sure you want to delete invoice #${saleToDelete?.sale?.invoiceNumber}?",
            confirmText = "Delete Invoice",
            isDangerous = true,
            onConfirm = {
                saleToDelete?.let { viewModel.deleteInvoice(it) }
                saleToDelete = null
            },
            onDismiss = { saleToDelete = null }
        )
    }

    if (showCartDialog) {
        CartCheckoutModal(
            viewModel = viewModel,
            cartItems = cartItems,
            customers = customers,
            currency = currency,
            onDismiss = { showCartDialog = false }
        )
    }
}

@Composable
private fun PosRegisterContent(
    viewModel: ShopViewModel,
    cartItems: List<CartItem>,
    mobiles: List<MobileItem>,
    accessories: List<AccessoryItem>,
    customers: List<Customer>,
    currency: String,
    onOpenScanner: () -> Unit,
    onOpenCartModal: () -> Unit
) {
    var catalogFilter by remember { mutableStateOf("ALL") } // ALL, MOBILES, ACCESSORIES
    var searchQuery by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val totalCartCount = cartItems.sumOf { it.quantity }

    val filteredMobiles = mobiles.filter { m ->
        (catalogFilter == "ALL" || catalogFilter == "MOBILES") &&
                (searchQuery.isBlank() ||
                        m.brand.contains(searchQuery, true) ||
                        m.model.contains(searchQuery, true) ||
                        m.barcode.contains(searchQuery, true) ||
                        m.imei.contains(searchQuery, true))
    }

    val filteredAccessories = accessories.filter { a ->
        (catalogFilter == "ALL" || catalogFilter == "ACCESSORIES") &&
                (searchQuery.isBlank() ||
                        a.name.contains(searchQuery, true) ||
                        a.category.contains(searchQuery, true) ||
                        a.barcode.contains(searchQuery, true))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top POS Search & Barcode Trigger
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search mobile, accessory, barcode..", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenScanner,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Catalog Category Filter
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All Stock", "MOBILES" to "Mobiles", "ACCESSORIES" to "Accessories").forEach { (key, label) ->
                        val isSel = catalogFilter == key
                        Box(
                            modifier = Modifier
                                .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(14.dp))
                                .clickable { catalogFilter = key }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Products Grid / List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMobiles.isNotEmpty()) {
                    item {
                        Text(text = "Smartphones & Mobiles", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(filteredMobiles, key = { "m_${it.id}" }) { mobile ->
                        PosProductItem(
                            title = "${mobile.brand} ${mobile.model}",
                            subtitle = "${mobile.ram}/${mobile.storage} • ${mobile.color} • Stock: ${mobile.quantity}",
                            barcode = mobile.barcode.ifBlank { mobile.imei },
                            price = mobile.salePrice,
                            quantity = mobile.quantity,
                            currency = currency,
                            icon = Icons.Default.PhoneAndroid,
                            onAddToCart = { viewModel.addMobileToCart(mobile) }
                        )
                    }
                }

                if (filteredAccessories.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Accessories & Parts", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(filteredAccessories, key = { "a_${it.id}" }) { acc ->
                        PosProductItem(
                            title = acc.name,
                            subtitle = "${acc.category} • Stock: ${acc.quantity}",
                            barcode = acc.barcode,
                            price = acc.salePrice,
                            quantity = acc.quantity,
                            currency = currency,
                            icon = Icons.Default.Headphones,
                            onAddToCart = { viewModel.addAccessoryToCart(acc) }
                        )
                    }
                }

                if (filteredMobiles.isEmpty() && filteredAccessories.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.PointOfSale,
                            title = "No Products in Catalog",
                            message = "Add mobiles or accessories to start selling."
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Bar
        if (cartItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onOpenCartModal() },
                colors = CardDefaults.cardColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$totalCartCount",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cart Total",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format("%.2f", subtotal)} $currency",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Checkout →",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PosProductItem(
    title: String,
    subtitle: String,
    barcode: String,
    price: Double,
    quantity: Int,
    currency: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    if (barcode.isNotBlank()) {
                        Text(
                            text = "[$barcode]",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format("%.2f", price)} $currency",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onAddToCart,
                    enabled = quantity > 0,
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (quantity > 0) GoldPrimary else Color.Gray, RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add to Cart", tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CartCheckoutModal(
    viewModel: ShopViewModel,
    cartItems: List<CartItem>,
    customers: List<Customer>,
    currency: String,
    onDismiss: () -> Unit
) {
    var customerName by remember { mutableStateOf("Cash Customer") }
    var customerPhone by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("0") }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

    var paidAmountStr by remember { mutableStateOf(grandTotal.toString()) }
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: grandTotal
    val remaining = (grandTotal - paidAmount).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Sale Cart (${cartItems.size} items)",
                            color = GoldPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text(stringRes("clear_cart"), color = DangerRed, fontSize = 12.sp)
                        }
                    }
                }

                // Cart items list
                items(cartItems.indices.toList()) { index ->
                    val item = cartItems[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.itemName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(text = "${String.format("%.2f", item.unitPrice)} $currency × ${item.quantity} = ${String.format("%.2f", item.totalPrice)}", color = GoldPrimary, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.updateCartItemQuantity(index, item.quantity - 1) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                            }
                            Text(text = "${item.quantity}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = { viewModel.updateCartItemQuantity(index, item.quantity + 1) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { viewModel.removeCartItem(index) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = DarkBorder)
                }

                // Customer Selection
                item {
                    Column {
                        Text(text = "Customer Info", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Customer quick pick chips
                        if (customers.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .background(if (customerName == "Cash Customer") GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(10.dp))
                                            .clickable {
                                                customerName = "Cash Customer"
                                                customerPhone = ""
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Walk-in Cash", color = if (customerName == "Cash Customer") Color.Black else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                items(customers) { c ->
                                    val isSel = customerName == c.name
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(10.dp))
                                            .clickable {
                                                customerName = c.name
                                                customerPhone = c.phone
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = c.name, color = if (isSel) Color.Black else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Phone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }

                // Financial Inputs (Discount & Paid)
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = {
                                discountStr = it
                                val d = it.toDoubleOrNull() ?: 0.0
                                val g = (subtotal - d).coerceAtLeast(0.0)
                                paidAmountStr = g.toString()
                            },
                            label = { Text("Discount ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = paidAmountStr,
                            onValueChange = { paidAmountStr = it },
                            label = { Text("Amount Paid ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Summary calculations
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B1B1F), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Subtotal:", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "${String.format("%.2f", subtotal)} $currency", color = TextPrimary, fontSize = 12.sp)
                        }
                        if (discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Discount:", color = TextSecondary, fontSize = 12.sp)
                                Text(text = "-${String.format("%.2f", discount)} $currency", color = DangerRed, fontSize = 12.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Grand Total:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${String.format("%.2f", grandTotal)} $currency", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Paid Amount:", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "${String.format("%.2f", paidAmount)} $currency", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (remaining > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Customer Balance:", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${String.format("%.2f", remaining)} $currency", color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Checkout & Cancel Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringRes("cancel"), color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.setCustomerDetails(customerName, customerPhone)
                                viewModel.setDiscount(discount)
                                viewModel.completeSale(paidAmount)
                                onDismiss()
                            },
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Complete & Print", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesHistoryContent(
    salesHistory: List<SaleWithItems>,
    currency: String,
    onViewReceipt: (SaleWithItems) -> Unit,
    onDeleteSale: (SaleWithItems) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredSales = salesHistory.filter { s ->
        searchQuery.isBlank() ||
                s.sale.invoiceNumber.contains(searchQuery, true) ||
                s.sale.customerName.contains(searchQuery, true) ||
                s.sale.customerPhone.contains(searchQuery, true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search
        Box(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search invoice number, customer..", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        if (filteredSales.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Receipt,
                title = "No Invoices Found",
                message = "Completed sales invoices will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSales, key = { it.sale.id }) { saleWithItems ->
                    val sale = saleWithItems.sale
                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(sale.date))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewReceipt(saleWithItems) },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(GoldContainer, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = sale.invoiceNumber, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = sale.customerName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { onDeleteSale(saleWithItems) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${saleWithItems.items.size} Items: " + saleWithItems.items.joinToString(", ") { "${it.itemName} (x${it.quantity})" },
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = dateStr, color = TextMuted, fontSize = 10.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Total: ${String.format("%.2f", sale.grandTotal)} $currency", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (sale.remainingAmount > 0) {
                                        Text(text = "Bal: ${String.format("%.2f", sale.remainingAmount)}", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
