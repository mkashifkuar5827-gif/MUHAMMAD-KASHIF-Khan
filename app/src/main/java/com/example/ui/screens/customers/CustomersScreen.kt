package com.example.ui.screens.customers

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.RepairJob
import com.example.data.model.SaleWithItems
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.locale.stringRes
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomersScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val sales by viewModel.salesState.collectAsStateWithLifecycle()
    val repairs by viewModel.repairsState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var customerForLedger by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter { c ->
        searchQuery.isBlank() ||
                c.name.contains(searchQuery, true) ||
                c.phone.contains(searchQuery, true) ||
                c.address.contains(searchQuery, true)
    }

    // Calculate customer balances
    val customerBalances = remember(customers, sales, repairs) {
        customers.associate { c ->
            val custSalesRemaining = sales
                .filter { it.sale.customerName.equals(c.name, true) || (c.phone.isNotBlank() && it.sale.customerPhone == c.phone) }
                .sumOf { it.sale.remainingAmount }
            val custRepairsRemaining = repairs
                .filter { it.customerName.equals(c.name, true) || (c.phone.isNotBlank() && it.customerPhone == c.phone) }
                .sumOf { it.remainingPayment }
            c.id to (custSalesRemaining + custRepairsRemaining)
        }
    }

    val totalBalanceDue = customerBalances.values.sum()

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
                            Icons.Default.People,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("customers_title"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            editingCustomer = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("add_customer"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringRes("search_customers_hint"), fontSize = 13.sp) },
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
            }

            // Summary Banner
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
                        Text(text = "Total Customers", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${customers.size}", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Balance Pending", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.2f", totalBalanceDue)} $currency", color = if (totalBalanceDue > 0) DangerRed else SuccessGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List
            if (filteredCustomers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "No Customers Found",
                    message = if (searchQuery.isNotBlank()) "No records match '$searchQuery'" else "Add customer profiles to track ledgers and balances.",
                    actionButtonText = stringRes("add_customer"),
                    onActionClick = {
                        editingCustomer = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        val balance = customerBalances[customer.id] ?: 0.0

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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(GoldContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = customer.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            if (customer.phone.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = customer.phone, color = TextSecondary, fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        Icons.Default.Call,
                                                        contentDescription = "Call",
                                                        tint = GoldPrimary,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable {
                                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                                                context.startActivity(intent)
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { customerForLedger = customer }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.History, contentDescription = "Ledger", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                editingCustomer = customer
                                                showAddEditDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { customerToDelete = customer }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                if (customer.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Address: ${customer.address}", color = TextMuted, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (balance > 0) "Balance Due: ${String.format("%.2f", balance)} $currency" else "No Outstanding Balance",
                                        color = if (balance > 0) DangerRed else SuccessGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "View Ledger →",
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { customerForLedger = customer }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        CustomerAddEditDialog(
            customer = editingCustomer,
            onDismiss = { showAddEditDialog = false },
            onSave = { toSave ->
                viewModel.saveCustomer(toSave)
                showAddEditDialog = false
            }
        )
    }

    if (customerToDelete != null) {
        ConfirmationDialog(
            title = stringRes("delete_item"),
            message = "Are you sure you want to delete customer profile for ${customerToDelete?.name}?",
            confirmText = stringRes("delete_item"),
            isDangerous = true,
            onConfirm = {
                customerToDelete?.let { viewModel.deleteCustomer(it) }
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null }
        )
    }

    if (customerForLedger != null) {
        CustomerLedgerDialog(
            customer = customerForLedger!!,
            sales = sales.filter { it.sale.customerName.equals(customerForLedger!!.name, true) || (customerForLedger!!.phone.isNotBlank() && it.sale.customerPhone == customerForLedger!!.phone) },
            repairs = repairs.filter { it.customerName.equals(customerForLedger!!.name, true) || (customerForLedger!!.phone.isNotBlank() && it.customerPhone == customerForLedger!!.phone) },
            currency = currency,
            onDismiss = { customerForLedger = null }
        )
    }
}

@Composable
private fun CustomerAddEditDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

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
                    text = if (customer == null) stringRes("add_customer") else stringRes("edit_item"),
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringRes("customer_name")) },
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringRes("phone")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringRes("address")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringRes("notes")) },
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
                            if (name.isNotBlank()) {
                                onSave(
                                    Customer(
                                        id = customer?.id ?: 0L,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        notes = notes.trim(),
                                        createdAt = customer?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
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

@Composable
private fun CustomerLedgerDialog(
    customer: Customer,
    sales: List<SaleWithItems>,
    repairs: List<RepairJob>,
    currency: String,
    onDismiss: () -> Unit
) {
    val totalSales = sales.sumOf { it.sale.grandTotal }
    val totalSalesPaid = sales.sumOf { it.sale.paidAmount }
    val totalSalesRemaining = sales.sumOf { it.sale.remainingAmount }

    val totalRepairs = repairs.sumOf { it.repairCost }
    val totalRepairsPaid = repairs.sumOf { it.advancePayment }
    val totalRepairsRemaining = repairs.sumOf { it.remainingPayment }

    val netBalance = totalSalesRemaining + totalRepairsRemaining

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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Customer Ledger: ${customer.name}",
                        color = GoldPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(text = "Phone: ${customer.phone}", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                item {
                    // Balance Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Total Purchases:", color = TextSecondary, fontSize = 12.sp)
                                Text(text = "${String.format("%.2f", totalSales)} $currency", color = TextPrimary, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Total Repairs:", color = TextSecondary, fontSize = 12.sp)
                                Text(text = "${String.format("%.2f", totalRepairs)} $currency", color = TextPrimary, fontSize = 12.sp)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Net Outstanding Balance:", color = if (netBalance > 0) DangerRed else SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${String.format("%.2f", netBalance)} $currency", color = if (netBalance > 0) DangerRed else SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Recent Sales
                if (sales.isNotEmpty()) {
                    item {
                        Text(text = "Purchases History (${sales.size})", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(sales) { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = s.sale.invoiceNumber, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(s.sale.date)), color = TextMuted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${String.format("%.2f", s.sale.grandTotal)} $currency", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (s.sale.remainingAmount > 0) {
                                    Text(text = "Bal: ${String.format("%.2f", s.sale.remainingAmount)}", color = DangerRed, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Recent Repairs
                if (repairs.isNotEmpty()) {
                    item {
                        Text(text = "Repair Jobs (${repairs.size})", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(repairs) { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "${r.jobCode} • ${r.mobileBrand} ${r.mobileModel}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${r.repairService} (${r.status})", color = TextMuted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${String.format("%.2f", r.repairCost)} $currency", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (r.remainingPayment > 0) {
                                    Text(text = "Bal: ${String.format("%.2f", r.remainingPayment)}", color = DangerRed, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringRes("cancel"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
