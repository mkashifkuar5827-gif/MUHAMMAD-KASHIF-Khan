package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaleWithItems
import com.example.data.model.ShopProfile
import com.example.ui.locale.stringRes
import com.example.ui.screens.reports.ReportPeriod
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.pdf.SalesReportPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailySalesPdfModal(
    allSales: List<SaleWithItems>,
    profile: ShopProfile,
    initialPeriod: ReportPeriod = ReportPeriod.TODAY,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    val currency = profile.currency

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
                val start = cal.timeInMillis
                Pair(start, System.currentTimeMillis())
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                Pair(start, System.currentTimeMillis())
            }
            ReportPeriod.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
        }
    }

    val filteredSales = remember(allSales, startTime, endTime) {
        allSales.filter { it.sale.date in startTime..endTime }
    }

    val totalSalesRevenue = remember(filteredSales) { filteredSales.sumOf { it.sale.grandTotal } }
    val totalPaid = remember(filteredSales) { filteredSales.sumOf { it.sale.paidAmount } }
    val totalDue = remember(filteredSales) { filteredSales.sumOf { it.sale.remainingAmount } }
    val totalItemsCount = remember(filteredSales) { filteredSales.sumOf { sale -> sale.items.sumOf { it.quantity } } }

    val periodTitle = remember(selectedPeriod) {
        when (selectedPeriod) {
            ReportPeriod.TODAY -> "Daily Sales Report (${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())})"
            ReportPeriod.YESTERDAY -> "Yesterday's Sales Report"
            ReportPeriod.THIS_WEEK -> "Weekly Sales Report"
            ReportPeriod.THIS_MONTH -> "Monthly Sales Report (${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())})"
            ReportPeriod.ALL_TIME -> "All-Time Comprehensive Sales Report"
        }
    }

    fun triggerPdfGeneration(action: (File) -> Unit) {
        isGenerating = true
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val periodSubtitle = when (selectedPeriod) {
                        ReportPeriod.TODAY -> SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
                        ReportPeriod.YESTERDAY -> {
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(cal.time)
                        }
                        ReportPeriod.THIS_WEEK -> "This Week (${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(startTime))} - ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(endTime))})"
                        ReportPeriod.THIS_MONTH -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                        ReportPeriod.ALL_TIME -> "Complete Transaction History"
                    }

                    SalesReportPdfGenerator.generateDailySalesPdf(
                        context = context,
                        sales = filteredSales,
                        profile = profile,
                        reportTitle = if (selectedPeriod == ReportPeriod.TODAY) "DAILY SALES REPORT" else "SALES & REVENUE REPORT",
                        periodSubtitle = periodSubtitle
                    )
                }
                generatedFile = file
                isGenerating = false
                action(file)
            } catch (e: Exception) {
                isGenerating = false
                Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = OnGoldContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringRes("daily_sales_pdf_report"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = stringRes("pdf_report_subtitle"),
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Period Selector Chips
                Text(
                    text = stringRes("report_period"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReportPeriod.entries) { period ->
                        val isSelected = period == selectedPeriod
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) GoldPrimary else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkBorder),
                            modifier = Modifier.clickable {
                                selectedPeriod = period
                                generatedFile = null
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = DarkSurface,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = stringRes(period.labelKey),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DarkSurface else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PDF Preview / Calculation Summary Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Shop Header Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.shopName.ifBlank { "Kashif Mobile & Repair" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                            Text(
                                text = "A4 PDF Document",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = DarkBorder
                        )

                        // 4 Mini Summary Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniPdfStat(
                                label = "Total Sales",
                                value = "${String.format(Locale.getDefault(), "%.2f", totalSalesRevenue)} $currency",
                                valueColor = GoldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            MiniPdfStat(
                                label = "Invoices",
                                value = "${filteredSales.size} ($totalItemsCount pcs)",
                                valueColor = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniPdfStat(
                                label = "Cash Received",
                                value = "${String.format(Locale.getDefault(), "%.2f", totalPaid)} $currency",
                                valueColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            MiniPdfStat(
                                label = "Balance Due",
                                value = "${String.format(Locale.getDefault(), "%.2f", totalDue)} $currency",
                                valueColor = if (totalDue > 0) Color(0xFFEF4444) else TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Generating formatted PDF report...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    // Share PDF Button (Primary)
                    Button(
                        onClick = {
                            triggerPdfGeneration { file ->
                                SalesReportPdfGenerator.sharePdfReport(
                                    context = context,
                                    pdfFile = file,
                                    title = "${profile.shopName} - $periodTitle"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("share_pdf"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Actions: View PDF & Print PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                triggerPdfGeneration { file ->
                                    SalesReportPdfGenerator.viewPdfReport(context, file)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringRes("view_pdf"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                triggerPdfGeneration { file ->
                                    SalesReportPdfGenerator.sharePdfReport(
                                        context = context,
                                        pdfFile = file,
                                        title = "${profile.shopName} - $periodTitle"
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringRes("print_receipt"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPdfStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(0.75.dp, DarkBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
