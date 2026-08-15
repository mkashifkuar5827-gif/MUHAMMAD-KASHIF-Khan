package com.example.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.RepairJob
import com.example.data.model.SaleWithItems
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.DailySalesPdfModal
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageStateManager
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.stringRes
import com.example.ui.screens.reports.ReportPeriod
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRepairing
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNavigate: (String) -> Unit,
    onOpenScanner: () -> Unit
) {
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val mobiles by viewModel.mobilesState.collectAsStateWithLifecycle()
    val accessories by viewModel.accessoriesState.collectAsStateWithLifecycle()
    val repairs by viewModel.repairsState.collectAsStateWithLifecycle()
    val sales by viewModel.salesState.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()
    val customers by viewModel.customersState.collectAsStateWithLifecycle()

    val currentLang = LocalAppLanguage.current
    var showLangMenu by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDailyPdfModal by remember { mutableStateOf(false) }

    val currency = profile.currency

    // Calculate today's start and end timestamps
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis

    // Aggregations
    val totalMobileQty = mobiles.sumOf { it.quantity }
    val totalMobileVal = mobiles.sumOf { it.quantity * it.salePrice }

    val totalAccessoryQty = accessories.sumOf { it.quantity }
    val totalAccessoryVal = accessories.sumOf { it.quantity * it.salePrice }

    val pendingRepairsCount = repairs.count { it.status.equals("PENDING", ignoreCase = true) || it.status.equals("REPAIRING", ignoreCase = true) }

    val todaySalesList = sales.filter { it.sale.date >= startOfToday }
    val todaySalesTotal = todaySalesList.sumOf { it.sale.grandTotal }

    val todayRepairsList = repairs.filter { it.dateReceived >= startOfToday }
    val todayRepairsIncome = todayRepairsList.sumOf { it.advancePayment }

    val totalCustomerBalance = sales.sumOf { it.sale.remainingAmount } + repairs.sumOf { it.remainingPayment }

    val todayExpensesTotal = expenses.filter { it.date >= startOfToday }.sumOf { it.amount }

    // Profit calculation for today
    val todaySalesProfit = todaySalesList.sumOf { saleWithItems ->
        saleWithItems.items.sumOf { item ->
            (item.unitPrice - item.purchasePrice) * item.quantity
        } - saleWithItems.sale.discount
    }
    val todayNetProfit = (todaySalesProfit + todayRepairsIncome - todayExpensesTotal).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11)),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Luxury Shop Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF241D0D), Color(0xFF151518))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Shop Logo / Icon
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, GoldPrimary, CircleShape)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_shop_logo),
                                        contentDescription = "Shop Logo",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = profile.shopName,
                                        color = GoldPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${stringRes("owner_name")}: ${profile.ownerName}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Language Selector & Scanner
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Fast language switcher button
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = GoldContainer.copy(alpha = 0.35f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.toggleLanguage() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Language,
                                            contentDescription = "Language",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = currentLang.flagEmoji,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onOpenScanner,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GoldPrimary, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan Barcode",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Offline status badge + address
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${profile.shopAddress}, ${profile.city}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF064E3B), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "100% Offline DB",
                                    color = Color(0xFF34D399),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringRes("quick_actions"),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    QuickActionButton(
                        title = stringRes("new_sale"),
                        icon = Icons.Default.PointOfSale,
                        color = GoldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("sales") }
                    )
                    QuickActionButton(
                        title = stringRes("new_repair"),
                        icon = Icons.Default.Build,
                        color = StatusRepairing,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("repairs") }
                    )
                    QuickActionButton(
                        title = stringRes("scan_barcode"),
                        icon = Icons.Default.QrCodeScanner,
                        color = GoldSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenScanner
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    QuickActionButton(
                        title = stringRes("nav_mobiles"),
                        icon = Icons.Default.PhoneAndroid,
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("mobiles") }
                    )
                    QuickActionButton(
                        title = stringRes("nav_accessories"),
                        icon = Icons.Default.Headphones,
                        color = Color(0xFFF472B6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("accessories") }
                    )
                    QuickActionButton(
                        title = stringRes("nav_rates"),
                        icon = Icons.Default.ReceiptLong,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("rates") }
                    )
                }
            }
        }

        // Key Business Metrics 2x4 Grid
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringRes("stock_summary"),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Row 1: Mobile Stock & Accessories Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = stringRes("total_mobile_stock"),
                        value = "$totalMobileQty ${stringRes("quantity")}",
                        subtitle = "Val: ${String.format("%.0f", totalMobileVal)} $currency",
                        icon = Icons.Default.PhoneAndroid,
                        accentColor = GoldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("mobiles") }
                    )
                    StatCard(
                        title = stringRes("total_accessories"),
                        value = "$totalAccessoryQty ${stringRes("quantity")}",
                        subtitle = "Val: ${String.format("%.0f", totalAccessoryVal)} $currency",
                        icon = Icons.Default.Headphones,
                        accentColor = Color(0xFFF472B6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("accessories") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Pending Repairs & Today's Sales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = stringRes("pending_repairs"),
                        value = "$pendingRepairsCount Jobs",
                        subtitle = "Active in lab",
                        icon = Icons.Default.Build,
                        accentColor = StatusPending,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("repairs") }
                    )
                    StatCard(
                        title = stringRes("today_sales"),
                        value = "${String.format("%.2f", todaySalesTotal)} $currency",
                        subtitle = "${todaySalesList.size} Invoices • PDF",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { showDailyPdfModal = true }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Today's Repair Income & Customer Balances
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = stringRes("today_repairs"),
                        value = "${String.format("%.2f", todayRepairsIncome)} $currency",
                        subtitle = "${todayRepairsList.size} Repairs today",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("daily_repairs") }
                    )
                    StatCard(
                        title = stringRes("customer_balance"),
                        value = "${String.format("%.2f", totalCustomerBalance)} $currency",
                        subtitle = "Pending to collect",
                        icon = Icons.Default.MoneyOff,
                        accentColor = Color(0xFFFB7185),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("customers") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 4: Today's Expenses & Today's Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = stringRes("today_expenses"),
                        value = "${String.format("%.2f", todayExpensesTotal)} $currency",
                        subtitle = "Shop costs",
                        icon = Icons.Default.ReceiptLong,
                        accentColor = Color(0xFFF87171),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("expenses") }
                    )
                    StatCard(
                        title = stringRes("today_profit"),
                        value = "${String.format("%.2f", todayNetProfit)} $currency",
                        subtitle = "Estimated Net",
                        icon = Icons.Default.TrendingUp,
                        accentColor = GoldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("reports") }
                    )
                }
            }
        }

        // Recent Repair Jobs Section
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringRes("recent_repairs"),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringRes("view_details"),
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate("repairs") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (repairs.isEmpty()) {
                    Text(
                        text = "No repair jobs recorded yet.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                } else {
                    repairs.take(3).forEach { repair ->
                        RepairDashboardItem(
                            repair = repair,
                            currency = currency,
                            onClick = { onNavigate("repairs") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Recent Invoices Section
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringRes("recent_sales"),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringRes("view_details"),
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate("sales") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (sales.isEmpty()) {
                    Text(
                        text = "No sales recorded yet. Start billing in POS!",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                } else {
                    sales.take(3).forEach { saleWithItems ->
                        SaleDashboardItem(
                            saleWithItems = saleWithItems,
                            currency = currency,
                            onClick = { onNavigate("sales") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showDailyPdfModal) {
        DailySalesPdfModal(
            allSales = sales,
            profile = profile,
            initialPeriod = ReportPeriod.TODAY,
            onDismiss = { showDailyPdfModal = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLang,
            onSelectLanguage = { lang ->
                viewModel.setLanguage(lang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(76.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RepairDashboardItem(
    repair: RepairJob,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
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
                    Text(
                        text = repair.jobCode,
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${repair.mobileBrand} ${repair.mobileModel}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${repair.customerName} • ${repair.repairService}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = repair.status)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${String.format("%.2f", repair.repairCost)} $currency",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SaleDashboardItem(
    saleWithItems: SaleWithItems,
    currency: String,
    onClick: () -> Unit
) {
    val sale = saleWithItems.sale
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(sale.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
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
                Text(
                    text = sale.invoiceNumber,
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${sale.customerName} • ${saleWithItems.items.size} item(s)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.2f", sale.grandTotal)} $currency",
                    color = SuccessGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (sale.remainingAmount > 0) {
                    Text(
                        text = "Bal: ${String.format("%.2f", sale.remainingAmount)}",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
