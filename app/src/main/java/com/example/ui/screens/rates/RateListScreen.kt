package com.example.ui.screens.rates

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.data.model.RateItem
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.locale.stringRes
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopViewModel

@Composable
fun RateListScreen(viewModel: ShopViewModel) {
    val rates by viewModel.ratesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, REPAIR_SERVICE, MOBILE, ACCESSORY

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingRate by remember { mutableStateOf<RateItem?>(null) }
    var rateToDelete by remember { mutableStateOf<RateItem?>(null) }

    val filteredRates = rates.filter { r ->
        val matchesType = selectedTypeFilter == "ALL" || r.type == selectedTypeFilter
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                r.title.lowercase().contains(query) ||
                r.category.lowercase().contains(query) ||
                r.notes.lowercase().contains(query)
        matchesType && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
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
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("rate_list_title"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            editingRate = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("add_rate"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringRes("search_rates_hint"), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
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

                // Filter tabs
                val types = listOf(
                    "ALL" to "All Rates",
                    "REPAIR_SERVICE" to "Repair Rates",
                    "MOBILE" to "Mobile Rates",
                    "ACCESSORY" to "Accessory Rates"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { (type, label) ->
                        val isSel = selectedTypeFilter == type
                        Box(
                            modifier = Modifier
                                .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(14.dp))
                                .clickable { selectedTypeFilter = type }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, color = if (isSel) Color.Black else TextSecondary, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Rates List
            if (filteredRates.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Rate Items Found",
                    message = if (searchQuery.isNotBlank()) "No rates match '$searchQuery'" else "Add standard repair rates and market rates.",
                    actionButtonText = stringRes("add_rate"),
                    onActionClick = {
                        editingRate = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRates, key = { it.id }) { rate ->
                        val icon = when (rate.type) {
                            "REPAIR_SERVICE" -> Icons.Default.Build
                            "MOBILE" -> Icons.Default.PhoneAndroid
                            else -> Icons.Default.Headphones
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(GoldContainer, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = rate.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (rate.category.isNotBlank() || rate.notes.isNotBlank()) {
                                            Text(
                                                text = "${rate.category} • ${rate.notes}".trim(' ', '•'),
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format("%.2f", if (rate.sellingRate > 0) rate.sellingRate else rate.standardRate)} $currency",
                                            color = GoldPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (rate.purchaseRate > 0) {
                                            Text(
                                                text = "Cost: ${String.format("%.2f", rate.purchaseRate)}",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            editingRate = rate
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { rateToDelete = rate },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        RateAddEditDialog(
            rate = editingRate,
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { toSave ->
                viewModel.saveRate(toSave)
                showAddEditDialog = false
            }
        )
    }

    if (rateToDelete != null) {
        ConfirmationDialog(
            title = stringRes("delete_item"),
            message = "Are you sure you want to delete rate for ${rateToDelete?.title}?",
            confirmText = stringRes("delete_item"),
            isDangerous = true,
            onConfirm = {
                rateToDelete?.let { viewModel.deleteRate(it) }
                rateToDelete = null
            },
            onDismiss = { rateToDelete = null }
        )
    }
}

@Composable
private fun RateAddEditDialog(
    rate: RateItem?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (RateItem) -> Unit
) {
    var type by remember { mutableStateOf(rate?.type ?: "REPAIR_SERVICE") }
    var title by remember { mutableStateOf(rate?.title ?: "") }
    var category by remember { mutableStateOf(rate?.category ?: "") }
    var sellingRateStr by remember { mutableStateOf(if (rate != null) rate.sellingRate.toString() else "") }
    var purchaseRateStr by remember { mutableStateOf(if (rate != null) rate.purchaseRate.toString() else "") }
    var notes by remember { mutableStateOf(rate?.notes ?: "") }

    val typeOptions = listOf(
        "REPAIR_SERVICE" to "Repair Service",
        "MOBILE" to "Mobile Phone",
        "ACCESSORY" to "Accessory"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (rate == null) stringRes("add_rate") else stringRes("edit_item"),
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Type selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    typeOptions.forEach { (t, label) ->
                        val isSel = type == t
                        Box(
                            modifier = Modifier
                                .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                                .clickable { type = t }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, color = if (isSel) Color.Black else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item / Service Name") },
                    placeholder = { Text("e.g. Display Replacement (OLED)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringRes("category")) },
                    placeholder = { Text("e.g. Screen, Battery, Charging") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellingRateStr,
                        onValueChange = { sellingRateStr = it },
                        label = { Text("Selling Rate ($currency)") },
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
                        value = purchaseRateStr,
                        onValueChange = { purchaseRateStr = it },
                        label = { Text("Cost Rate ($currency)") },
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringRes("notes")) },
                    placeholder = { Text("e.g. 1 week test warranty") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                            if (title.isNotBlank()) {
                                val sRate = sellingRateStr.toDoubleOrNull() ?: 0.0
                                val pRate = purchaseRateStr.toDoubleOrNull() ?: 0.0
                                onSave(
                                    RateItem(
                                        id = rate?.id ?: 0L,
                                        type = type,
                                        title = title.trim(),
                                        category = category.trim(),
                                        sellingRate = sRate,
                                        standardRate = sRate,
                                        purchaseRate = pRate,
                                        notes = notes.trim(),
                                        updatedDate = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
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
