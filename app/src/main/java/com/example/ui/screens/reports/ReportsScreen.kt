package com.example.ui.screens.reports

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Expense
import com.example.data.model.RepairJob
import com.example.data.model.SaleWithItems
import com.example.data.model.ShopProfile
import com.example.ui.components.DailySalesPdfModal
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PrintShareHelper
import com.example.ui.locale.stringRes
import com.example.util.pdf.SalesReportPdfGenerator
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDelivered
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

enum class ReportPeriod(val labelKey: String) {
    TODAY("period_today"),
    YESTERDAY("period_yesterday"),
    THIS_WEEK("period_this_week"),
    THIS_MONTH("period_this_month"),
    ALL_TIME("period_all_time")
}

@Composable
fun ReportsScreen(viewModel: ShopViewModel) {
    val sales by viewModel.salesState.collectAsStateWithLifecycle()
    val repairs by viewModel.repairsState.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Financial Summary, 1: Sales, 2: Repairs, 3: Expenses
    var showPdfModal by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Calculate time bounds for filter
    val (startTime, endTime) = remember(selectedPeriod) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            ReportPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            ReportPeriod.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            ReportPeriod.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }

    // Filter datasets
    val filteredSales = remember(sales, startTime, endTime) {
        sales.filter { it.sale.date in startTime..endTime }
    }

    val filteredRepairs = remember(repairs, startTime, endTime) {
        repairs.filter { it.dateReceived in startTime..endTime }
    }

    val filteredExpenses = remember(expenses, startTime, endTime) {
        expenses.filter { it.date in startTime..endTime }
    }

    // Metrics calculations
    val totalSalesRevenue = filteredSales.sumOf { it.sale.grandTotal }
    val totalSalesPaid = filteredSales.sumOf { it.sale.paidAmount }
    val totalSalesDue = filteredSales.sumOf { it.sale.remainingAmount }

    val totalCostOfGoods = filteredSales.sumOf { saleWithItems ->
        saleWithItems.items.sumOf { it.purchasePrice * it.quantity }
    }
    val salesGrossProfit = (totalSalesRevenue - totalCostOfGoods).coerceAtLeast(0.0)

    val totalRepairsCharged = filteredRepairs.sumOf { it.repairCost }
    val totalRepairsCollected = filteredRepairs.sumOf { it.advancePayment }
    val totalRepairsDue = filteredRepairs.sumOf { it.remainingPayment }

    val totalExpensesAmount = filteredExpenses.sumOf { it.amount }

    val totalShopRevenue = totalSalesRevenue + totalRepairsCharged
    val totalShopIncomeReceived = totalSalesPaid + totalRepairsCollected
    val netShopProfit = salesGrossProfit + totalRepairsCharged - totalExpensesAmount

    val shareReportText = remember(
        selectedPeriod, totalSalesRevenue, salesGrossProfit, totalRepairsCharged,
        totalExpensesAmount, netShopProfit, currency, profile
    ) {
        buildString {
            appendLine("════════════════════════════════")
            appendLine("  ${profile.shopName.uppercase()}")
            appendLine("  FINANCIAL & BUSINESS REPORT")
            appendLine("════════════════════════════════")
            appendLine("Period: ${selectedPeriod.name.replace('_', ' ')}")
            appendLine("Date: ${SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())}")
            appendLine("--------------------------------")
            appendLine("SALES SUMMARY:")
            appendLine("  • Invoices Count: ${filteredSales.size}")
            appendLine("  • Total Sales Value: $currency ${String.format("%.2f", totalSalesRevenue)}")
            appendLine("  • Cost of Goods Sold: $currency ${String.format("%.2f", totalCostOfGoods)}")
            appendLine("  • Sales Gross Profit: $currency ${String.format("%.2f", salesGrossProfit)}")
            appendLine("  • Unpaid Balance: $currency ${String.format("%.2f", totalSalesDue)}")
            appendLine("--------------------------------")
            appendLine("REPAIRS SUMMARY:")
            appendLine("  • Total Repair Jobs: ${filteredRepairs.size}")
            appendLine("  • Total Repair Charges: $currency ${String.format("%.2f", totalRepairsCharged)}")
            appendLine("  • Payment Collected: $currency ${String.format("%.2f", totalRepairsCollected)}")
            appendLine("  • Outstanding Due: $currency ${String.format("%.2f", totalRepairsDue)}")
            appendLine("--------------------------------")
            appendLine("EXPENSES & OVERHEADS:")
            appendLine("  • Total Recorded Expenses: $currency ${String.format("%.2f", totalExpensesAmount)}")
            appendLine("--------------------------------")
            appendLine("NET ESTIMATED PROFIT: $currency ${String.format("%.2f", netShopProfit)}")
            appendLine("════════════════════════════════")
            appendLine("Generated by ${profile.shopName} App")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Reports",
                        tint = OnGoldContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringRes("nav_reports"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${stringRes("report_period")}: ${stringRes(selectedPeriod.labelKey)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // PDF Daily Report Generator Button
                IconButton(
                    onClick = { showPdfModal = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Report",
                        tint = OnGoldContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "${profile.shopName} Report - ${selectedPeriod.name}")
                            putExtra(Intent.EXTRA_TEXT, shareReportText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Business Report"))
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        PrintShareHelper.printHtmlReceipt(
                            context = context,
                            jobName = "Business_Report_${selectedPeriod.name}",
                            htmlContent = generateHtmlReport(
                                profile = profile,
                                periodName = selectedPeriod.name.replace('_', ' '),
                                sales = filteredSales,
                                repairs = filteredRepairs,
                                expenses = filteredExpenses,
                                totalSalesRevenue = totalSalesRevenue,
                                totalCostOfGoods = totalCostOfGoods,
                                salesGrossProfit = salesGrossProfit,
                                totalRepairsCharged = totalRepairsCharged,
                                totalExpensesAmount = totalExpensesAmount,
                                netShopProfit = netShopProfit,
                                currency = currency
                            )
                        )
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print Report",
                        tint = DarkSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Period Selection Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ReportPeriod.entries) { period ->
                val isSelected = period == selectedPeriod
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) GoldPrimary else DarkSurfaceVariant,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.clickable { selectedPeriod = period }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DarkSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = stringRes(period.labelKey),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DarkSurface else TextSecondary
                        )
                    }
                }
            }
        }

        // Navigation Tabs for Report sections
        val tabs = listOf(
            stringRes("financial_summary"),
            stringRes("nav_sales"),
            stringRes("nav_repairs"),
            stringRes("nav_expenses")
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurfaceVariant,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) GoldPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // FINANCIAL SUMMARY & NET PROFIT
                    item {
                        // Hero Net Profit Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                if (netShopProfit >= 0) Color(0xFF1E3A2F) else Color(0xFF3E1C1C),
                                                DarkSurfaceVariant
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        if (netShopProfit >= 0) SuccessGreen.copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = stringRes("net_profit"),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "$currency ${String.format("%.2f", netShopProfit)}",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (netShopProfit >= 0) SuccessGreen else DangerRed
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (netShopProfit >= 0) SuccessGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (netShopProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (netShopProfit >= 0) SuccessGreen else DangerRed,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = DarkBorder)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "Total Revenue", fontSize = 11.sp, color = TextMuted)
                                            Text(
                                                text = "$currency ${String.format("%.2f", totalShopRevenue)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "Gross Profit", fontSize = 11.sp, color = TextMuted)
                                            Text(
                                                text = "$currency ${String.format("%.2f", salesGrossProfit + totalRepairsCharged)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldPrimary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "Total Expenses", fontSize = 11.sp, color = TextMuted)
                                            Text(
                                                text = "$currency ${String.format("%.2f", totalExpensesAmount)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DangerRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Key Metrics Grid
                    item {
                        // PDF Quick Action Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldContainer.copy(alpha = 0.22f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = DarkSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = stringRes("daily_sales_pdf_report"),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "A4 PDF formatted report with header & totals",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Button(
                                    onClick = { showPdfModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringRes("share_pdf"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Key Metrics Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ReportMetricCard(
                                title = stringRes("today_sales"),
                                value = "$currency ${String.format("%.2f", totalSalesRevenue)}",
                                subtitle = "${filteredSales.size} Invoices",
                                icon = Icons.Default.PointOfSale,
                                iconColor = GoldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricCard(
                                title = stringRes("repairs_income"),
                                value = "$currency ${String.format("%.2f", totalRepairsCharged)}",
                                subtitle = "${filteredRepairs.size} Jobs",
                                icon = Icons.Default.Build,
                                iconColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ReportMetricCard(
                                title = stringRes("customer_balance"),
                                value = "$currency ${String.format("%.2f", totalSalesDue + totalRepairsDue)}",
                                subtitle = "Uncollected Credit",
                                icon = Icons.Default.AccountBalanceWallet,
                                iconColor = Color(0xFFFFA726),
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricCard(
                                title = stringRes("nav_expenses"),
                                value = "$currency ${String.format("%.2f", totalExpensesAmount)}",
                                subtitle = "${filteredExpenses.size} Records",
                                icon = Icons.Default.MoneyOff,
                                iconColor = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Profit Margin Breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Revenue & Margin Composition",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val totalPosGross = totalSalesRevenue + totalRepairsCharged
                                val salesShare = if (totalPosGross > 0) (totalSalesRevenue / totalPosGross).toFloat() else 0f
                                val repairsShare = if (totalPosGross > 0) (totalRepairsCharged / totalPosGross).toFloat() else 0f

                                Text(
                                    text = "Sales vs Repairs Income Ratio",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkBorder)
                                ) {
                                    if (salesShare > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(salesShare.coerceAtLeast(0.01f))
                                                .height(8.dp)
                                                .background(GoldPrimary)
                                        )
                                    }
                                    if (repairsShare > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(repairsShare.coerceAtLeast(0.01f))
                                                .height(8.dp)
                                                .background(SuccessGreen)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Sales: ${(salesShare * 100).toInt()}% ($currency ${String.format("%.2f", totalSalesRevenue)})",
                                        fontSize = 11.sp,
                                        color = GoldPrimary
                                    )
                                    Text(
                                        text = "Repairs: ${(repairsShare * 100).toInt()}% ($currency ${String.format("%.2f", totalRepairsCharged)})",
                                        fontSize = 11.sp,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // SALES DETAILED BREAKDOWN
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Sales Summary (${stringRes(selectedPeriod.labelKey)})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportDetailRow("Total Invoices", "${filteredSales.size}")
                                ReportDetailRow("Gross Sales Total", "$currency ${String.format("%.2f", totalSalesRevenue)}")
                                ReportDetailRow("Cost of Goods Sold (Purchase Value)", "$currency ${String.format("%.2f", totalCostOfGoods)}")
                                ReportDetailRow("Gross Margin / Profit", "$currency ${String.format("%.2f", salesGrossProfit)}", highlightColor = SuccessGreen)
                                ReportDetailRow("Discounts Given", "$currency ${String.format("%.2f", filteredSales.sumOf { it.sale.discount })}")
                                ReportDetailRow("Cash Received", "$currency ${String.format("%.2f", totalSalesPaid)}")
                                ReportDetailRow("Remaining Customer Balance", "$currency ${String.format("%.2f", totalSalesDue)}", highlightColor = if (totalSalesDue > 0) DangerRed else TextPrimary)
                            }
                        }
                    }

                    item {
                        // PDF Banner for Sales
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldContainer.copy(alpha = 0.22f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = DarkSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = stringRes("daily_sales_pdf_report"),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Itemized breakdown & revenue totals in PDF",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Button(
                                    onClick = { showPdfModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringRes("share_pdf"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Invoices in Period (${filteredSales.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            OutlinedButton(
                                onClick = { showPdfModal = true },
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.75.dp, GoldPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", fontSize = 10.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (filteredSales.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.PointOfSale,
                                title = "No sales in selected period",
                                message = "Sales and invoices recorded during this period will appear here."
                            )
                        }
                    } else {
                        items(filteredSales) { saleWithItems ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${saleWithItems.sale.invoiceNumber} • ${saleWithItems.sale.customerName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(saleWithItems.sale.date)),
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${saleWithItems.items.size} item(s): " + saleWithItems.items.joinToString(", ") { "${it.quantity}x ${it.itemName}" },
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$currency ${String.format("%.2f", saleWithItems.sale.grandTotal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                        if (saleWithItems.sale.remainingAmount > 0) {
                                            Text(
                                                text = "Due: $currency ${String.format("%.2f", saleWithItems.sale.remainingAmount)}",
                                                fontSize = 11.sp,
                                                color = DangerRed,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
                                            Text(
                                                text = "PAID",
                                                fontSize = 10.sp,
                                                color = SuccessGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // REPAIRS BREAKDOWN
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Repairs Service Analytics",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportDetailRow("Total Jobs Received", "${filteredRepairs.size}")
                                ReportDetailRow("Total Service Charges", "$currency ${String.format("%.2f", totalRepairsCharged)}", highlightColor = SuccessGreen)
                                ReportDetailRow("Advance / Payments Collected", "$currency ${String.format("%.2f", totalRepairsCollected)}")
                                ReportDetailRow("Pending Customer Dues", "$currency ${String.format("%.2f", totalRepairsDue)}", highlightColor = if (totalRepairsDue > 0) DangerRed else TextPrimary)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = DarkBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                val pendingCount = filteredRepairs.count { it.status.equals("PENDING", true) }
                                val repairingCount = filteredRepairs.count { it.status.equals("REPAIRING", true) }
                                val completedCount = filteredRepairs.count { it.status.equals("COMPLETED", true) }
                                val deliveredCount = filteredRepairs.count { it.status.equals("DELIVERED", true) }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatusCountChip("Pending", pendingCount, StatusPending)
                                    StatusCountChip("Repairing", repairingCount, StatusRepairing)
                                    StatusCountChip("Ready", completedCount, StatusCompleted)
                                    StatusCountChip("Delivered", deliveredCount, StatusDelivered)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Repair Records in Period (${filteredRepairs.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (filteredRepairs.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Build,
                                title = "No repairs in selected period",
                                message = "Repair jobs received in this timeframe will be listed here."
                            )
                        }
                    } else {
                        items(filteredRepairs) { repair ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${repair.jobCode} • ${repair.customerName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${repair.mobileBrand} ${repair.mobileModel} - ${repair.customerProblem}",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Status: ${repair.status}",
                                            fontSize = 11.sp,
                                            color = when (repair.status.uppercase()) {
                                                "DELIVERED" -> StatusDelivered
                                                "COMPLETED" -> StatusCompleted
                                                "REPAIRING" -> StatusRepairing
                                                else -> StatusPending
                                            },
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$currency ${String.format("%.2f", repair.repairCost)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                        if (repair.remainingPayment > 0) {
                                            Text(
                                                text = "Due: $currency ${String.format("%.2f", repair.remainingPayment)}",
                                                fontSize = 11.sp,
                                                color = DangerRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // EXPENSES BREAKDOWN
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Shop Expense Categories",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val expensesByCategory = filteredExpenses.groupBy { it.category }
                                if (expensesByCategory.isEmpty()) {
                                    Text(
                                        text = "No expenses recorded for this period.",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                } else {
                                    expensesByCategory.forEach { (category, list) ->
                                        val catTotal = list.sumOf { it.amount }
                                        val percentage = if (totalExpensesAmount > 0) (catTotal / totalExpensesAmount).toFloat() else 0f
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = category, fontSize = 12.sp, color = TextPrimary)
                                                Text(
                                                    text = "$currency ${String.format("%.2f", catTotal)} (${(percentage * 100).toInt()}%)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DangerRed
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { percentage },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = DangerRed,
                                                trackColor = DarkBorder,
                                                strokeCap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Individual Expenses (${filteredExpenses.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (filteredExpenses.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.MoneyOff,
                                title = "No expenses recorded",
                                message = "Rent, electricity, parts, or food expenses for this period will appear here."
                            )
                        }
                    } else {
                        items(filteredExpenses) { expense ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = expense.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${expense.category} • " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date)),
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                        if (expense.description.isNotBlank()) {
                                            Text(
                                                text = expense.description,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$currency ${String.format("%.2f", expense.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPdfModal) {
        DailySalesPdfModal(
            allSales = sales,
            profile = profile,
            initialPeriod = selectedPeriod,
            onDismiss = { showPdfModal = false }
        )
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ReportDetailRow(
    title: String,
    value: String,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = highlightColor
        )
    }
}

@Composable
fun StatusCountChip(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}

fun generateHtmlReport(
    profile: ShopProfile,
    periodName: String,
    sales: List<SaleWithItems>,
    repairs: List<RepairJob>,
    expenses: List<Expense>,
    totalSalesRevenue: Double,
    totalCostOfGoods: Double,
    salesGrossProfit: Double,
    totalRepairsCharged: Double,
    totalExpensesAmount: Double,
    netShopProfit: Double,
    currency: String
): String {
    val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>${profile.shopName} - Business Report</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; color: #222; }
                .header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 20px; }
                .shop-name { font-size: 22px; font-weight: bold; }
                .report-title { font-size: 16px; color: #555; margin-top: 4px; }
                .summary-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                .summary-table th, .summary-table td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }
                .summary-table th { background-color: #f2f2f2; }
                .highlight { font-weight: bold; color: #1e7e34; }
                .expense { color: #dc3545; }
                .net-box { background: #e8f5e9; border: 2px solid #4caf50; padding: 15px; text-align: center; font-size: 18px; font-weight: bold; border-radius: 8px; margin-bottom: 20px; }
                .footer { text-align: center; font-size: 11px; color: #777; margin-top: 30px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="shop-name">${profile.shopName}</div>
                <div class="report-title">Business & Financial Report ($periodName)</div>
                <div>Generated on: $dateStr | Owner: ${profile.ownerName}</div>
            </div>

            <div class="net-box">
                NET ESTIMATED SHOP PROFIT: $currency ${String.format("%.2f", netShopProfit)}
            </div>

            <h3>Financial Summary</h3>
            <table class="summary-table">
                <tr><th>Metric</th><th>Amount ($currency)</th></tr>
                <tr><td>Gross Sales Revenue (${sales.size} Invoices)</td><td>${String.format("%.2f", totalSalesRevenue)}</td></tr>
                <tr><td>Cost of Goods Sold (Stock Purchase Cost)</td><td>${String.format("%.2f", totalCostOfGoods)}</td></tr>
                <tr><td>Sales Gross Margin / Profit</td><td class="highlight">${String.format("%.2f", salesGrossProfit)}</td></tr>
                <tr><td>Repair Income (${repairs.size} Jobs)</td><td class="highlight">${String.format("%.2f", totalRepairsCharged)}</td></tr>
                <tr><td>Total Shop Expenses (${expenses.size} Records)</td><td class="expense">-${String.format("%.2f", totalExpensesAmount)}</td></tr>
                <tr style="background:#f9f9f9;font-weight:bold;"><td>Net Profit</td><td class="highlight">${String.format("%.2f", netShopProfit)}</td></tr>
            </table>

            <div class="footer">
                ${profile.shopAddress}, ${profile.city} • WhatsApp: ${profile.whatsappNumber}<br>
                Thank you for using Kashif Mobile & Repair Management System
            </div>
        </body>
        </html>
    """.trimIndent()
}
