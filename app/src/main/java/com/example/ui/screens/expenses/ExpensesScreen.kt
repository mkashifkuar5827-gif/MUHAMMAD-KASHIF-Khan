package com.example.ui.screens.expenses

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.model.ExpenseItem
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(viewModel: ShopViewModel) {
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseItem?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseItem?>(null) }

    val expenseCategories = listOf(
        "All", "Shop Rent", "Electricity Bill", "Tea & Refreshments",
        "Staff Salary", "Tools & Spare Parts", "Courier & Freight", "Marketing", "Other"
    )

    val filteredExpenses = expenses.filter { exp ->
        val matchesCat = selectedCategoryFilter == "All" || exp.category.equals(selectedCategoryFilter, ignoreCase = true)
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                exp.name.lowercase().contains(query) ||
                exp.category.lowercase().contains(query) ||
                exp.description.lowercase().contains(query)
        matchesCat && matchesQuery
    }

    // Calculations
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val todayStart = cal.timeInMillis

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val monthStart = cal.timeInMillis

    val totalExpensesSum = filteredExpenses.sumOf { it.amount }
    val todayExpensesSum = expenses.filter { it.date >= todayStart }.sumOf { it.amount }
    val monthExpensesSum = expenses.filter { it.date >= monthStart }.sumOf { it.amount }

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
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("expenses_title"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            editingExpense = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("add_expense"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringRes("search_expenses_hint"), fontSize = 13.sp) },
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

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(expenseCategories) { cat ->
                        val isSel = cat == selectedCategoryFilter
                        Box(
                            modifier = Modifier
                                .background(if (isSel) GoldPrimary else DarkSurfaceVariant, RoundedCornerShape(14.dp))
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, color = if (isSel) Color.Black else TextSecondary, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Summary Card
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
                        Text(text = "Today Expense", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.0f", todayExpensesSum)} $currency", color = DangerRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "This Month", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.0f", monthExpensesSum)} $currency", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Expenses", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${String.format("%.0f", totalExpensesSum)} $currency", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "No Expenses Found",
                    message = if (searchQuery.isNotBlank()) "No records match '$searchQuery'" else "Track daily shop operational costs.",
                    actionButtonText = stringRes("add_expense"),
                    onActionClick = {
                        editingExpense = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { exp ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(exp.date))

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(GoldContainer, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = exp.category, color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = exp.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (exp.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = exp.description, color = TextMuted, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = dateStr, color = TextMuted, fontSize = 10.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${String.format("%.2f", exp.amount)} $currency",
                                        color = DangerRed,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            editingExpense = exp
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { expenseToDelete = exp },
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
        ExpenseAddEditDialog(
            expense = editingExpense,
            categories = expenseCategories.filter { it != "All" },
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { toSave ->
                viewModel.saveExpense(toSave)
                showAddEditDialog = false
            }
        )
    }

    if (expenseToDelete != null) {
        ConfirmationDialog(
            title = stringRes("delete_item"),
            message = "Are you sure you want to delete expense '${expenseToDelete?.name}'?",
            confirmText = stringRes("delete_item"),
            isDangerous = true,
            onConfirm = {
                expenseToDelete?.let { viewModel.deleteExpense(it) }
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}

@Composable
private fun ExpenseAddEditDialog(
    expense: ExpenseItem?,
    categories: List<String>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseItem) -> Unit
) {
    var name by remember { mutableStateOf(expense?.name ?: "") }
    var category by remember { mutableStateOf(expense?.category ?: "Shop Rent") }
    var amountStr by remember { mutableStateOf(if (expense != null) expense.amount.toString() else "") }
    var description by remember { mutableStateOf(expense?.description ?: "") }

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
                    text = if (expense == null) stringRes("add_expense") else stringRes("edit_item"),
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Category chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSel = category == cat
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Title / Reason") },
                    placeholder = { Text("e.g. Electricity Bill for July") },
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
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringRes("notes")) },
                    placeholder = { Text("Paid via Cash / Bank / Details") },
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
                                    ExpenseItem(
                                        id = expense?.id ?: 0L,
                                        name = name.trim(),
                                        category = category.trim(),
                                        amount = amountStr.toDoubleOrNull() ?: 0.0,
                                        date = expense?.date ?: System.currentTimeMillis(),
                                        description = description.trim()
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
