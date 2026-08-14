package com.example.ui.screens.accessories

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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccessoryItem
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
fun AccessoriesScreen(
    viewModel: ShopViewModel,
    onNavigateToPos: () -> Unit
) {
    val accessories by viewModel.accessoriesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingAccessory by remember { mutableStateOf<AccessoryItem?>(null) }
    var accessoryToDelete by remember { mutableStateOf<AccessoryItem?>(null) }
    var barcodeToDisplay by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showScannerForSearch by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Charger", "Handsfree", "Mobile Cover", "Glass Protector",
        "Battery", "Cable", "Power Bank", "Earphones", "Memory Card", "Adapter", "Other"
    )

    val filteredAccessories = accessories.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                item.name.lowercase().contains(query) ||
                item.category.lowercase().contains(query) ||
                item.barcode.lowercase().contains(query)
        matchesCategory && matchesQuery
    }

    val totalQty = filteredAccessories.sumOf { it.quantity }
    val totalCost = filteredAccessories.sumOf { it.quantity * it.purchasePrice }
    val totalValue = filteredAccessories.sumOf { it.quantity * it.salePrice }
    val expectedProfit = totalValue - totalCost

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Title & Search
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
                            Icons.Default.Headphones,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("accessories_title"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            editingAccessory = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("add_accessory"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box with Scanner
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringRes("search_accessories_hint"), fontSize = 13.sp) },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Category Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) GoldPrimary else DarkSurfaceVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Valuation Summary
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
                        Text(text = "$totalQty Items", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

            // List of Accessories
            if (filteredAccessories.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Headphones,
                    title = "No Accessories Found",
                    message = if (searchQuery.isNotBlank()) "No items match '$searchQuery'" else "Add chargers, cables, screen protectors to track stock.",
                    actionButtonText = stringRes("add_accessory"),
                    onActionClick = {
                        editingAccessory = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAccessories, key = { it.id }) { acc ->
                        AccessoryCardItem(
                            accessory = acc,
                            currency = currency,
                            onQuantityChange = { delta -> viewModel.adjustAccessoryStock(acc, delta) },
                            onEdit = {
                                editingAccessory = acc
                                showAddEditDialog = true
                            },
                            onDelete = { accessoryToDelete = acc },
                            onShowBarcode = {
                                barcodeToDisplay = Pair(acc.barcode, acc.name)
                            },
                            onAddToCart = {
                                viewModel.addAccessoryToCart(acc)
                                onNavigateToPos()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AccessoryAddEditDialog(
            accessory = editingAccessory,
            categories = categories.filter { it != "All" },
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { toSave ->
                viewModel.saveAccessory(toSave)
                showAddEditDialog = false
            }
        )
    }

    if (accessoryToDelete != null) {
        ConfirmationDialog(
            title = stringRes("delete_item"),
            message = "Are you sure you want to delete ${accessoryToDelete?.name}?",
            confirmText = stringRes("delete_item"),
            isDangerous = true,
            onConfirm = {
                accessoryToDelete?.let { viewModel.deleteAccessory(it) }
                accessoryToDelete = null
            },
            onDismiss = { accessoryToDelete = null }
        )
    }

    if (barcodeToDisplay != null) {
        BarcodeDisplayDialog(
            code = barcodeToDisplay!!.first,
            title = barcodeToDisplay!!.second,
            subtitle = "Scan with barcode scanner to sell in POS",
            onDismiss = { barcodeToDisplay = null }
        )
    }

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
private fun AccessoryCardItem(
    accessory: AccessoryItem,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(GoldContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = accessory.category,
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = accessory.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
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

            if (accessory.barcode.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Barcode: ${accessory.barcode}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock Adjuster
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
                            text = "${String.format("%.2f", accessory.salePrice)} $currency",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Cost: ${String.format("%.2f", accessory.purchasePrice)} $currency | Margin: +${String.format("%.2f", accessory.salePrice - accessory.purchasePrice)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

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
                                if (accessory.quantity <= 2) DangerRed.copy(alpha = 0.2f) else DarkSurface,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${accessory.quantity}",
                            color = if (accessory.quantity <= 2) DangerRed else TextPrimary,
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
                        enabled = accessory.quantity > 0,
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (accessory.quantity > 0) GoldPrimary else Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Sell", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessoryAddEditDialog(
    accessory: AccessoryItem?,
    categories: List<String>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (AccessoryItem) -> Unit
) {
    var name by remember { mutableStateOf(accessory?.name ?: "") }
    var category by remember { mutableStateOf(accessory?.category ?: "Charger") }
    var barcode by remember { mutableStateOf(accessory?.barcode ?: "") }
    var purchasePriceStr by remember { mutableStateOf(if (accessory != null) accessory.purchasePrice.toString() else "") }
    var salePriceStr by remember { mutableStateOf(if (accessory != null) accessory.salePrice.toString() else "") }
    var quantityStr by remember { mutableStateOf(if (accessory != null) accessory.quantity.toString() else "1") }
    var notes by remember { mutableStateOf(accessory?.notes ?: "") }

    var showScannerForBarcode by remember { mutableStateOf(false) }

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
                        text = if (accessory == null) stringRes("add_accessory") else stringRes("edit_item"),
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Category Selector
                item {
                    Column {
                        Text(text = stringRes("category"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val isSel = category.equals(cat, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                                        .clickable { category = cat }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = cat, color = if (isSel) Color.Black else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Item Name
                item {
                    Column {
                        Text(text = stringRes("item_name"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. Apple 20W Fast Charger") },
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

                // Barcode + Auto-Generate + Scan
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
                                    barcode = BarcodeUtils.generateRandomBarcode("KMR-ACC")
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            placeholder = { Text("Scan or enter barcode") },
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

                // Prices
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

                // Quantity
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

                // Notes
                item {
                    Column {
                        Text(text = stringRes("notes"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("e.g. Original box, 6 months warranty") },
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

                // Action Buttons
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
                                if (name.isNotBlank()) {
                                    val itemToSave = AccessoryItem(
                                        id = accessory?.id ?: 0L,
                                        name = name.trim(),
                                        category = category.trim(),
                                        barcode = barcode.trim(),
                                        purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0,
                                        salePrice = salePriceStr.toDoubleOrNull() ?: 0.0,
                                        quantity = quantityStr.toIntOrNull() ?: 1,
                                        dateAdded = accessory?.dateAdded ?: System.currentTimeMillis(),
                                        notes = notes.trim()
                                    )
                                    onSave(itemToSave)
                                }
                            },
                            enabled = name.isNotBlank(),
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
