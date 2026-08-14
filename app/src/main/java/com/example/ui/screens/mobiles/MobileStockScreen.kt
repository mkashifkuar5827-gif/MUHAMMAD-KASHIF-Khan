package com.example.ui.screens.mobiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MobileItem
import com.example.ui.components.BarcodeDisplayDialog
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
import com.example.util.BarcodeUtils
import com.example.viewmodel.ShopViewModel

@Composable
fun MobileStockScreen(
    viewModel: ShopViewModel,
    onNavigateToPos: () -> Unit
) {
    val mobiles by viewModel.mobilesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var searchQuery by remember { mutableStateOf("") }
    var selectedBrandFilter by remember { mutableStateOf("All") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingMobile by remember { mutableStateOf<MobileItem?>(null) }

    var mobileToDelete by remember { mutableStateOf<MobileItem?>(null) }
    var barcodeToDisplay by remember { mutableStateOf<Pair<String, String>?>(null) } // code to title

    var showScannerForSearch by remember { mutableStateOf(false) }

    val brands = remember(mobiles) {
        listOf("All") + mobiles.map { it.brand }.filter { it.isNotBlank() }.distinct()
    }

    val filteredMobiles = mobiles.filter { item ->
        val matchesBrand = selectedBrandFilter == "All" || item.brand.equals(selectedBrandFilter, ignoreCase = true)
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                item.brand.lowercase().contains(query) ||
                item.model.lowercase().contains(query) ||
                item.imei.lowercase().contains(query) ||
                item.barcode.lowercase().contains(query) ||
                item.color.lowercase().contains(query)
        matchesBrand && matchesQuery
    }

    // Totals
    val totalQty = filteredMobiles.sumOf { it.quantity }
    val totalCost = filteredMobiles.sumOf { it.quantity * it.purchasePrice }
    val totalSaleValue = filteredMobiles.sumOf { it.quantity * it.salePrice }
    val expectedProfit = totalSaleValue - totalCost

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Title & Top Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("mobiles_title"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            editingMobile = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("add_mobile"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box with Scanner
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringRes("search_mobiles_hint"), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                            IconButton(onClick = { showScannerForSearch = true }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = GoldPrimary)
                            }
                        }
                    },
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

                // Brand Filter Chips
                if (brands.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(brands) { brand ->
                            val isSelected = brand == selectedBrandFilter
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) GoldPrimary else DarkSurfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedBrandFilter = brand }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = brand,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Valuation Summary Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Stock", color = TextMuted, fontSize = 11.sp)
                        Text(text = "$totalQty Units", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Cost", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.0f", totalCost)} $currency", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Expected Profit", color = TextMuted, fontSize = 11.sp)
                        Text(text = "+${String.format("%.0f", expectedProfit)} $currency", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Mobiles List
            if (filteredMobiles.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PhoneAndroid,
                    title = "No Mobile Stock Found",
                    message = if (searchQuery.isNotBlank()) "No mobile phones match '$searchQuery'" else "Add new smartphones to start tracking inventory.",
                    actionButtonText = stringRes("add_mobile"),
                    onActionClick = {
                        editingMobile = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMobiles, key = { it.id }) { mobile ->
                        MobileCardItem(
                            mobile = mobile,
                            currency = currency,
                            onQuantityChange = { delta -> viewModel.adjustMobileStock(mobile, delta) },
                            onEdit = {
                                editingMobile = mobile
                                showAddEditDialog = true
                            },
                            onDelete = { mobileToDelete = mobile },
                            onShowBarcode = {
                                barcodeToDisplay = Pair(
                                    mobile.barcode.ifBlank { mobile.imei },
                                    "${mobile.brand} ${mobile.model}"
                                )
                            },
                            onAddToCart = {
                                viewModel.addMobileToCart(mobile)
                                onNavigateToPos()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        MobileAddEditDialog(
            mobile = editingMobile,
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { mobileToSave ->
                viewModel.saveMobile(mobileToSave)
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (mobileToDelete != null) {
        ConfirmationDialog(
            title = stringRes("delete_item"),
            message = "Are you sure you want to delete ${mobileToDelete?.brand} ${mobileToDelete?.model}?",
            confirmText = stringRes("delete_item"),
            isDangerous = true,
            onConfirm = {
                mobileToDelete?.let { viewModel.deleteMobile(it) }
                mobileToDelete = null
            },
            onDismiss = { mobileToDelete = null }
        )
    }

    // Barcode Viewer Dialog
    if (barcodeToDisplay != null) {
        BarcodeDisplayDialog(
            code = barcodeToDisplay!!.first,
            title = barcodeToDisplay!!.second,
            subtitle = "Scan with barcode scanner to sell in POS",
            onDismiss = { barcodeToDisplay = null }
        )
    }

    // Barcode Scanner for Search
    if (showScannerForSearch) {
        BarcodeScannerModal(
            onDismiss = { showScannerForSearch = false },
            onBarcodeScanned = { scannedCode ->
                showScannerForSearch = false
                searchQuery = scannedCode
            }
        )
    }
}

@Composable
private fun MobileCardItem(
    mobile: MobileItem,
    currency: String,
    onQuantityChange: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowBarcode: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Brand & Model + Actions
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
                        Text(
                            text = mobile.brand,
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mobile.model,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onShowBarcode, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "Barcode", tint = GoldSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Specs Row: RAM, Storage, Color
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (mobile.ram.isNotBlank() || mobile.storage.isNotBlank()) {
                    Text(
                        text = "${mobile.ram} / ${mobile.storage}".trim(' ', '/'),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (mobile.color.isNotBlank()) {
                    Text(
                        text = "• Color: ${mobile.color}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (mobile.imei.isNotBlank() || mobile.barcode.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mobile.imei.isNotBlank()) {
                        Text(
                            text = "IMEI: ${mobile.imei}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    if (mobile.barcode.isNotBlank()) {
                        Text(
                            text = "Barcode: ${mobile.barcode}",
                            color = GoldPrimary.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock Counter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sale: ",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${String.format("%.2f", mobile.salePrice)} $currency",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Cost: ${String.format("%.2f", mobile.purchasePrice)} $currency | Margin: +${String.format("%.2f", mobile.salePrice - mobile.purchasePrice)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Quantity Adjuster Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(-1) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(DarkSurface, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (mobile.quantity <= 1) DangerRed.copy(alpha = 0.2f) else DarkSurface,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${mobile.quantity}",
                            color = if (mobile.quantity <= 1) DangerRed else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { onQuantityChange(1) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(DarkSurface, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onAddToCart,
                        enabled = mobile.quantity > 0,
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (mobile.quantity > 0) GoldPrimary else Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Sell", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileAddEditDialog(
    mobile: MobileItem?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (MobileItem) -> Unit
) {
    var brand by remember { mutableStateOf(mobile?.brand ?: "") }
    var model by remember { mutableStateOf(mobile?.model ?: "") }
    var ram by remember { mutableStateOf(mobile?.ram ?: "8GB") }
    var storage by remember { mutableStateOf(mobile?.storage ?: "256GB") }
    var color by remember { mutableStateOf(mobile?.color ?: "") }
    var imei by remember { mutableStateOf(mobile?.imei ?: "") }
    var barcode by remember { mutableStateOf(mobile?.barcode ?: "") }
    var purchasePriceStr by remember { mutableStateOf(if (mobile != null) mobile.purchasePrice.toString() else "") }
    var salePriceStr by remember { mutableStateOf(if (mobile != null) mobile.salePrice.toString() else "") }
    var quantityStr by remember { mutableStateOf(if (mobile != null) mobile.quantity.toString() else "1") }
    var notes by remember { mutableStateOf(mobile?.notes ?: "") }

    var showScannerForImei by remember { mutableStateOf(false) }
    var showScannerForBarcode by remember { mutableStateOf(false) }

    val brandPresets = listOf("Apple", "Samsung", "Xiaomi", "Infinix", "Tecno", "Realme", "Vivo", "Oppo", "OnePlus", "Honor")
    val ramPresets = listOf("4GB", "6GB", "8GB", "12GB", "16GB")
    val storagePresets = listOf("64GB", "128GB", "256GB", "512GB", "1TB")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (mobile == null) stringRes("add_mobile") else stringRes("edit_item"),
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Brand Presets & Field
                item {
                    Column {
                        Text(text = stringRes("brand"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(brandPresets) { b ->
                                val isSel = brand.equals(b, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                                        .clickable { brand = b }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = b, color = if (isSel) Color.Black else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            placeholder = { Text("Brand name e.g. Samsung") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Model Field
                item {
                    Column {
                        Text(text = stringRes("model"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            placeholder = { Text("Model e.g. Galaxy S24 Ultra") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // RAM & Storage Selection
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringRes("ram"), color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ram,
                                onValueChange = { ram = it },
                                placeholder = { Text("8GB") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringRes("storage"), color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = storage,
                                onValueChange = { storage = it },
                                placeholder = { Text("256GB") },
                                singleLine = true,
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

                // Color Field
                item {
                    Column {
                        Text(text = stringRes("color"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            placeholder = { Text("e.g. Titanium Black, Blue") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // IMEI with Scanner
                item {
                    Column {
                        Text(text = stringRes("imei"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = imei,
                            onValueChange = { imei = it },
                            placeholder = { Text("15-digit IMEI or SN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showScannerForImei = true }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan IMEI", tint = GoldPrimary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Barcode with Auto-Generate and Scanner
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringRes("barcode"), color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "Auto Generate Code",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    barcode = BarcodeUtils.generateRandomBarcode("KMR-MOB")
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            placeholder = { Text("Scan or auto-generate") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showScannerForBarcode = true }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = GoldPrimary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Prices & Quantity
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${stringRes("purchase_price")} ($currency)", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = purchasePriceStr,
                                onValueChange = { purchasePriceStr = it },
                                placeholder = { Text("0.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${stringRes("sale_price")} ($currency)", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = salePriceStr,
                                onValueChange = { salePriceStr = it },
                                placeholder = { Text("0.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
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

                item {
                    Column {
                        Text(text = stringRes("quantity"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            placeholder = { Text("1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Notes Field
                item {
                    Column {
                        Text(text = stringRes("notes"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("e.g. Official warranty, includes silicone cover") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                if (brand.isNotBlank() && model.isNotBlank()) {
                                    val itemToSave = MobileItem(
                                        id = mobile?.id ?: 0L,
                                        brand = brand.trim(),
                                        model = model.trim(),
                                        ram = ram.trim(),
                                        storage = storage.trim(),
                                        color = color.trim(),
                                        imei = imei.trim(),
                                        barcode = barcode.trim(),
                                        purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0,
                                        salePrice = salePriceStr.toDoubleOrNull() ?: 0.0,
                                        quantity = quantityStr.toIntOrNull() ?: 1,
                                        dateAdded = mobile?.dateAdded ?: System.currentTimeMillis(),
                                        notes = notes.trim()
                                    )
                                    onSave(itemToSave)
                                }
                            },
                            enabled = brand.isNotBlank() && model.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringRes("save"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showScannerForImei) {
        BarcodeScannerModal(
            onDismiss = { showScannerForImei = false },
            onBarcodeScanned = { scannedCode ->
                showScannerForImei = false
                imei = scannedCode
            }
        )
    }

    if (showScannerForBarcode) {
        BarcodeScannerModal(
            onDismiss = { showScannerForBarcode = false },
            onBarcodeScanned = { scannedCode ->
                showScannerForBarcode = false
                barcode = scannedCode
            }
        )
    }
}
