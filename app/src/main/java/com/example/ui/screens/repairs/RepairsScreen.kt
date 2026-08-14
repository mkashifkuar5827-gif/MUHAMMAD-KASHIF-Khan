package com.example.ui.screens.repairs

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile
import com.example.ui.components.BarcodeDisplayDialog
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PrintShareHelper
import com.example.ui.components.StatusBadge
import com.example.ui.locale.stringRes
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRepairing
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BarcodeUtils
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepairsScreen(
    viewModel: ShopViewModel,
    onNavigateToDaily: () -> Unit
) {
    val context = LocalContext.current
    val repairs by viewModel.repairsState.collectAsStateWithLifecycle()
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val rates by viewModel.ratesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingRepair by remember { mutableStateOf<RepairJob?>(null) }
    var repairToDelete by remember { mutableStateOf<RepairJob?>(null) }
    var repairForPayment by remember { mutableStateOf<RepairJob?>(null) }
    var barcodeToDisplay by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showScannerForSearch by remember { mutableStateOf(false) }

    val statusFilters = listOf("ALL", "PENDING", "REPAIRING", "COMPLETED", "DELIVERED", "CANCELLED")

    val filteredRepairs = repairs.filter { item ->
        val matchesStatus = selectedStatusFilter == "ALL" || item.status.equals(selectedStatusFilter, ignoreCase = true)
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                item.jobCode.lowercase().contains(query) ||
                item.customerName.lowercase().contains(query) ||
                item.customerPhone.lowercase().contains(query) ||
                item.mobileBrand.lowercase().contains(query) ||
                item.mobileModel.lowercase().contains(query) ||
                item.imei.lowercase().contains(query) ||
                item.barcode.lowercase().contains(query) ||
                item.repairService.lowercase().contains(query) ||
                item.customerProblem.lowercase().contains(query)
        matchesStatus && matchesQuery
    }

    val totalCostSum = filteredRepairs.sumOf { it.repairCost }
    val totalAdvanceSum = filteredRepairs.sumOf { it.advancePayment }
    val totalRemainingSum = filteredRepairs.sumOf { it.remainingPayment }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
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
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("repairs_title"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        OutlinedButton(
                            onClick = onNavigateToDaily,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringRes("daily_repairs"), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                editingRepair = null
                                showAddEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringRes("add_repair"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box with Scanner
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringRes("search_repairs_hint"), fontSize = 13.sp) },
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

                // Status Filter Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(statusFilters) { status ->
                        val isSelected = status == selectedStatusFilter
                        val labelKey = when (status) {
                            "PENDING" -> "status_pending"
                            "REPAIRING" -> "status_repairing"
                            "COMPLETED" -> "status_completed"
                            "DELIVERED" -> "status_delivered"
                            "CANCELLED" -> "status_cancelled"
                            else -> "all"
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) GoldPrimary else DarkSurfaceVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedStatusFilter = status }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringRes(labelKey),
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Financial Summary Banner
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
                        Text(text = "Total Jobs", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${filteredRepairs.size}", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Advance Paid", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.0f", totalAdvanceSum)} $currency", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Remaining", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.0f", totalRemainingSum)} $currency", color = DangerRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Repairs List
            if (filteredRepairs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Build,
                    title = "No Repair Records Found",
                    message = if (searchQuery.isNotBlank()) "No repair orders match '$searchQuery'" else "Receive customer mobile phones and print claim slips.",
                    actionButtonText = stringRes("add_repair"),
                    onActionClick = {
                        editingRepair = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRepairs, key = { it.id }) { repair ->
                        RepairCardItem(
                            repair = repair,
                            profile = profile,
                            onStatusChange = { newStatus -> viewModel.updateRepairStatus(repair, newStatus) },
                            onReceivePayment = { repairForPayment = repair },
                            onEdit = {
                                editingRepair = repair
                                showAddEditDialog = true
                            },
                            onDelete = { repairToDelete = repair },
                            onShowBarcode = {
                                barcodeToDisplay = Pair(repair.barcode.ifBlank { repair.jobCode }, "Claim Slip ${repair.jobCode}")
                            },
                            onShareSlip = {
                                val slipText = PrintShareHelper.formatRepairReceiptText(repair, profile)
                                PrintShareHelper.shareText(context, slipText, "Repair Token ${repair.jobCode}")
                            },
                            onCallCustomer = {
                                if (repair.customerPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${repair.customerPhone}"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        RepairAddEditDialog(
            repair = editingRepair,
            customers = customers,
            rates = rates.filter { it.type == "REPAIR_SERVICE" },
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { toSave ->
                viewModel.saveRepair(toSave)
                showAddEditDialog = false
            }
        )
    }

    if (repairForPayment != null) {
        ReceiveRepairPaymentDialog(
            repair = repairForPayment!!,
            currency = currency,
            onDismiss = { repairForPayment = null },
            onConfirmPayment = { paymentAmount ->
                viewModel.receiveRepairBalance(repairForPayment!!, paymentAmount)
                repairForPayment = null
            }
        )
    }

    if (repairToDelete != null) {
        ConfirmationDialog(
            title = stringRes("delete_item"),
            message = "Are you sure you want to delete repair token ${repairToDelete?.jobCode}?",
            confirmText = stringRes("delete_item"),
            isDangerous = true,
            onConfirm = {
                repairToDelete?.let { viewModel.deleteRepair(it) }
                repairToDelete = null
            },
            onDismiss = { repairToDelete = null }
        )
    }

    if (barcodeToDisplay != null) {
        BarcodeDisplayDialog(
            code = barcodeToDisplay!!.first,
            title = barcodeToDisplay!!.second,
            subtitle = "Show this barcode to verify claim on collection",
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
private fun RepairCardItem(
    repair: RepairJob,
    profile: ShopProfile,
    onStatusChange: (String) -> Unit,
    onReceivePayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowBarcode: () -> Unit,
    onShareSlip: () -> Unit,
    onCallCustomer: () -> Unit
) {
    val currency = profile.currency
    var showStatusDropdown by remember { mutableStateOf(false) }
    val statuses = listOf("PENDING", "REPAIRING", "COMPLETED", "DELIVERED", "CANCELLED")

    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(repair.dateReceived))
    val expDelStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(repair.expectedDeliveryDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Bar: Job Code + Status + Actions
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
                            text = repair.jobCode,
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .clickable { showStatusDropdown = true }
                        ) {
                            StatusBadge(status = repair.status)
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st, fontWeight = if (st == repair.status) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        onStatusChange(st)
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onShowBarcode, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "Barcode", tint = GoldSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShareSlip, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share Slip", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer & Mobile Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${repair.mobileBrand} ${repair.mobileModel}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${repair.customerName} ${if (repair.customerPhone.isNotBlank()) "(${repair.customerPhone})" else ""}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (repair.customerPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Call",
                                tint = GoldPrimary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onCallCustomer() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Problem & Service box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Fault: ${repair.customerProblem}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (repair.repairService.isNotBlank()) {
                        Text(
                            text = "Service: ${repair.repairService}",
                            color = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }
                    if (repair.repairDetails.isNotBlank()) {
                        Text(
                            text = "Details: ${repair.repairDetails}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dates Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Received: $dateStr", color = TextMuted, fontSize = 10.sp)
                Text(text = "Exp. Delivery: $expDelStr", color = TextMuted, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Balance Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B1B1F), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Cost: ${String.format("%.2f", repair.repairCost)} $currency",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Adv: ${String.format("%.2f", repair.advancePayment)}",
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (repair.remainingPayment > 0) {
                        Text(
                            text = "Bal Remaining: ${String.format("%.2f", repair.remainingPayment)} $currency",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Fully Paid",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (repair.remainingPayment > 0) {
                    Button(
                        onClick = onReceivePayment,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RepairAddEditDialog(
    repair: RepairJob?,
    customers: List<Customer>,
    rates: List<com.example.data.model.RateItem>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (RepairJob) -> Unit
) {
    var customerName by remember { mutableStateOf(repair?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(repair?.customerPhone ?: "") }
    var mobileBrand by remember { mutableStateOf(repair?.mobileBrand ?: "") }
    var mobileModel by remember { mutableStateOf(repair?.mobileModel ?: "") }
    var imei by remember { mutableStateOf(repair?.imei ?: "") }
    var customerProblem by remember { mutableStateOf(repair?.customerProblem ?: "") }
    var repairDetails by remember { mutableStateOf(repair?.repairDetails ?: "") }
    var repairService by remember { mutableStateOf(repair?.repairService ?: "") }
    var repairCostStr by remember { mutableStateOf(if (repair != null) repair.repairCost.toString() else "") }
    var advancePaymentStr by remember { mutableStateOf(if (repair != null) repair.advancePayment.toString() else "0") }
    var status by remember { mutableStateOf(repair?.status ?: "PENDING") }
    var notes by remember { mutableStateOf(repair?.notes ?: "") }

    var showScannerForImei by remember { mutableStateOf(false) }

    val faultPresets = listOf(
        "Display Broken / No Touch",
        "Charging Port Loose / Not Charging",
        "Battery Draining Fast",
        "Water Damage / Dead Phone",
        "Software Stuck on Logo / FRP Lock",
        "Speaker / Mic Not Working",
        "Back Glass Broken",
        "Camera Blurry / Black Screen",
        "Network / No Service Issue"
    )

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
                        text = if (repair == null) stringRes("add_repair") else stringRes("edit_item"),
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Customer Name & Phone
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringRes("customer_name"), color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                placeholder = { Text("Customer Name") },
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
                            Text(text = stringRes("phone"), color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                placeholder = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                // Mobile Brand & Model
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringRes("brand"), color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = mobileBrand,
                                onValueChange = { mobileBrand = it },
                                placeholder = { Text("Apple, Samsung..") },
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
                            Text(text = stringRes("model"), color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = mobileModel,
                                onValueChange = { mobileModel = it },
                                placeholder = { Text("iPhone 13, A54..") },
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

                // IMEI with Scanner
                item {
                    Column {
                        Text(text = stringRes("imei"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = imei,
                            onValueChange = { imei = it },
                            placeholder = { Text("15-digit IMEI (optional)") },
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

                // Common Fault Presets
                item {
                    Column {
                        Text(text = stringRes("customer_problem"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(faultPresets) { fault ->
                                Box(
                                    modifier = Modifier
                                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                                        .clickable {
                                            customerProblem = fault
                                            if (repairService.isBlank()) {
                                                repairService = fault.split("/").first().trim()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = fault, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customerProblem,
                            onValueChange = { customerProblem = it },
                            placeholder = { Text("Describe problem or defect") },
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

                // Service presets from Rate List
                if (rates.isNotEmpty()) {
                    item {
                        Column {
                            Text(text = "Standard Repair Services", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(rates) { rate ->
                                    Box(
                                        modifier = Modifier
                                            .background(GoldContainer, RoundedCornerShape(12.dp))
                                            .clickable {
                                                repairService = rate.title
                                                if (repairCostStr.isBlank() || repairCostStr == "0") {
                                                    repairCostStr = rate.sellingRate.toString()
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "${rate.title} (${rate.sellingRate} $currency)", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Repair Service & Details
                item {
                    Column {
                        Text(text = stringRes("repair_details"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = repairDetails,
                            onValueChange = { repairDetails = it },
                            placeholder = { Text("e.g. Replaced OLED panel with waterproof adhesive") },
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

                // Repair Cost & Advance Payment
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${stringRes("repair_cost")} ($currency)", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = repairCostStr,
                                onValueChange = { repairCostStr = it },
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
                            Text(text = "${stringRes("advance_payment")} ($currency)", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = advancePaymentStr,
                                onValueChange = { advancePaymentStr = it },
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

                // Status
                item {
                    Column {
                        Text(text = stringRes("status"), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        val statuses = listOf("PENDING", "REPAIRING", "COMPLETED", "DELIVERED", "CANCELLED")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(statuses) { st ->
                                val isSel = status.equals(st, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                                        .clickable { status = st }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = st, color = if (isSel) Color.Black else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
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
                            placeholder = { Text("Warranty terms or customer requests") },
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
                                if (customerName.isNotBlank() && (mobileBrand.isNotBlank() || mobileModel.isNotBlank())) {
                                    val cost = repairCostStr.toDoubleOrNull() ?: 0.0
                                    val advance = advancePaymentStr.toDoubleOrNull() ?: 0.0
                                    val toSave = RepairJob(
                                        id = repair?.id ?: 0L,
                                        jobCode = repair?.jobCode ?: "",
                                        customerName = customerName.trim(),
                                        customerPhone = customerPhone.trim(),
                                        mobileBrand = mobileBrand.trim(),
                                        mobileModel = mobileModel.trim(),
                                        imei = imei.trim(),
                                        barcode = repair?.barcode ?: "",
                                        customerProblem = customerProblem.trim(),
                                        repairDetails = repairDetails.trim(),
                                        repairService = repairService.trim().ifBlank { "Mobile Repair" },
                                        repairCost = cost,
                                        advancePayment = advance,
                                        remainingPayment = (cost - advance).coerceAtLeast(0.0),
                                        dateReceived = repair?.dateReceived ?: System.currentTimeMillis(),
                                        expectedDeliveryDate = repair?.expectedDeliveryDate ?: (System.currentTimeMillis() + 86400000L * 2),
                                        status = status,
                                        notes = notes.trim()
                                    )
                                    onSave(toSave)
                                }
                            },
                            enabled = customerName.isNotBlank(),
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
}

@Composable
private fun ReceiveRepairPaymentDialog(
    repair: RepairJob,
    currency: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf(repair.remainingPayment.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Receive Repair Payment",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Token: ${repair.jobCode} • ${repair.customerName}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "Remaining Balance: ${String.format("%.2f", repair.remainingPayment)} $currency",
                    color = DangerRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount Received") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirmPayment(amt)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
