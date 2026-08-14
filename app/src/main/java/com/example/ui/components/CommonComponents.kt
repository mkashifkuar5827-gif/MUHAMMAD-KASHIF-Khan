package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RepairJob
import com.example.data.model.SaleWithItems
import com.example.data.model.ShopProfile
import com.example.ui.locale.stringRes
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRepairing
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BarcodeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = GoldPrimary,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, labelKey) = when (status.uppercase()) {
        "PENDING" -> Triple(StatusPending.copy(alpha = 0.2f), StatusPending, "status_pending")
        "REPAIRING" -> Triple(StatusRepairing.copy(alpha = 0.2f), StatusRepairing, "status_repairing")
        "COMPLETED" -> Triple(StatusCompleted.copy(alpha = 0.2f), StatusCompleted, "status_completed")
        "DELIVERED" -> Triple(StatusDelivered.copy(alpha = 0.2f), StatusDelivered, "status_delivered")
        "CANCELLED" -> Triple(StatusCancelled.copy(alpha = 0.2f), StatusCancelled, "status_cancelled")
        else -> Triple(Color.Gray.copy(alpha = 0.2f), Color.LightGray, status)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringRes(labelKey),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BarcodeDisplayDialog(
    code: String,
    title: String,
    subtitle: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(code) {
        BarcodeUtils.generateBarcodeBitmap(code, width = 600, height = 220)
    }

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
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barcode Container with white background for perfect contrast
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Barcode $code",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    } else {
                        Text(text = code, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = code,
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$title\nBarcode: $code\nKASHIF MOBILE AND REPAIR")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Barcode"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("share_receipt"))
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
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

@Composable
fun ConfirmationDialog(
    title: String = stringRes("confirm"),
    message: String,
    confirmText: String = stringRes("confirm"),
    cancelText: String = stringRes("cancel"),
    isDangerous: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDangerous) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(text = message, color = TextSecondary, fontSize = 14.sp)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDangerous) DangerRed else GoldPrimary,
                    contentColor = if (isDangerous) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = cancelText, color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(GoldContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = actionButtonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

object PrintShareHelper {

    fun formatSalesReceiptText(saleWithItems: SaleWithItems, profile: ShopProfile): String {
        val sale = saleWithItems.sale
        val items = saleWithItems.items
        val currency = profile.currency
        val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(sale.date))

        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("   ${profile.shopName.uppercase()}   ")
        sb.appendLine("   Owner: ${profile.ownerName}   ")
        sb.appendLine("   Tel: ${profile.phoneNumber}   ")
        sb.appendLine("   Email: ${profile.emailAddress}   ")
        sb.appendLine("   ${profile.shopAddress}, ${profile.city}   ")
        sb.appendLine("================================")
        sb.appendLine("INVOICE: ${sale.invoiceNumber}")
        sb.appendLine("DATE: $dateStr")
        sb.appendLine("CUSTOMER: ${sale.customerName}")
        if (sale.customerPhone.isNotBlank()) {
            sb.appendLine("PHONE: ${sale.customerPhone}")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("%-18s %3s %9s", "ITEM", "QTY", "TOTAL"))
        sb.appendLine("--------------------------------")

        for (item in items) {
            val itemLine = if (item.itemName.length > 18) item.itemName.substring(0, 15) + ".." else item.itemName
            val lineTotal = "${String.format("%.2f", item.totalPrice)} $currency"
            sb.appendLine(String.format("%-18s %3d %9s", itemLine, item.quantity, lineTotal))
            if (item.barcode.isNotBlank()) {
                sb.appendLine("  [${item.barcode}]")
            }
        }

        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("Subtotal:       %.2f %s", sale.subtotal, currency))
        if (sale.discount > 0) {
            sb.appendLine(String.format("Discount:      -%.2f %s", sale.discount, currency))
        }
        sb.appendLine(String.format("GRAND TOTAL:    %.2f %s", sale.grandTotal, currency))
        sb.appendLine(String.format("PAID:           %.2f %s", sale.paidAmount, currency))
        sb.appendLine(String.format("REMAINING:      %.2f %s", sale.remainingAmount, currency))
        sb.appendLine("================================")
        if (profile.invoiceTerms.isNotBlank()) {
            sb.appendLine(profile.invoiceTerms)
            sb.appendLine("================================")
        }
        sb.appendLine("Thank you for your business!")
        return sb.toString()
    }

    fun formatRepairReceiptText(repair: RepairJob, profile: ShopProfile): String {
        val currency = profile.currency
        val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(repair.dateReceived))
        val delDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(repair.expectedDeliveryDate))

        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("   ${profile.shopName.uppercase()}   ")
        sb.appendLine("     REPAIR CLAIM TOKEN         ")
        sb.appendLine("   Owner: ${profile.ownerName}   ")
        sb.appendLine("   Tel: ${profile.phoneNumber}   ")
        sb.appendLine("================================")
        sb.appendLine("TOKEN / JOB #: ${repair.jobCode}")
        sb.appendLine("DATE RECEIVED: $dateStr")
        sb.appendLine("CUSTOMER: ${repair.customerName}")
        sb.appendLine("PHONE: ${repair.customerPhone}")
        sb.appendLine("DEVICE: ${repair.mobileBrand} ${repair.mobileModel}")
        if (repair.imei.isNotBlank()) {
            sb.appendLine("IMEI: ${repair.imei}")
        }
        if (repair.barcode.isNotBlank()) {
            sb.appendLine("BARCODE: ${repair.barcode}")
        }
        sb.appendLine("SERVICE: ${repair.repairService}")
        sb.appendLine("FAULT/PROBLEM: ${repair.customerProblem}")
        sb.appendLine("REPAIR DETAILS: ${repair.repairDetails}")
        sb.appendLine("EXP. DELIVERY: $delDateStr")
        sb.appendLine("STATUS: ${repair.status}")
        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("TOTAL COST:     %.2f %s", repair.repairCost, currency))
        sb.appendLine(String.format("ADVANCE PAID:   %.2f %s", repair.advancePayment, currency))
        sb.appendLine(String.format("REMAINING:      %.2f %s", repair.remainingPayment, currency))
        sb.appendLine("================================")
        if (profile.invoiceTerms.isNotBlank()) {
            sb.appendLine(profile.invoiceTerms)
            sb.appendLine("================================")
        }
        sb.appendLine("Please bring this token for collection.")
        return sb.toString()
    }

    fun shareText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, title))
    }

    fun printHtmlReceipt(context: Context, htmlContent: String, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateSalesHtml(saleWithItems: SaleWithItems, profile: ShopProfile): String {
        val sale = saleWithItems.sale
        val items = saleWithItems.items
        val currency = profile.currency
        val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(sale.date))

        val itemsHtml = StringBuilder()
        for (item in items) {
            itemsHtml.append("""
                <tr>
                    <td style="padding:6px 0; border-bottom:1px solid #eee;">
                        <strong>${item.itemName}</strong><br/>
                        <span style="font-size:10px; color:#666;">Code: ${item.barcode}</span>
                    </td>
                    <td style="padding:6px 0; text-align:center; border-bottom:1px solid #eee;">${item.quantity}</td>
                    <td style="padding:6px 0; text-align:right; border-bottom:1px solid #eee;">${String.format("%.2f", item.unitPrice)}</td>
                    <td style="padding:6px 0; text-align:right; border-bottom:1px solid #eee;">${String.format("%.2f", item.totalPrice)} $currency</td>
                </tr>
            """.trimIndent())
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin:0; padding:20px; color:#222; font-size:13px; }
                    .header { text-align:center; margin-bottom:15px; border-bottom:2px dashed #333; padding-bottom:10px; }
                    .header h2 { margin:0 0 5px 0; font-size:18px; color:#000; }
                    .meta { margin-bottom:12px; font-size:12px; }
                    table { width:100%; border-collapse:collapse; margin-bottom:12px; font-size:12px; }
                    th { text-align:left; border-bottom:1px solid #333; padding-bottom:4px; font-size:11px; }
                    .totals { border-top:1px solid #333; padding-top:6px; font-size:13px; }
                    .totals div { display:flex; justify-content:space-between; margin-bottom:4px; }
                    .grand { font-size:16px; font-weight:bold; color:#000; border-top:1px dashed #333; padding-top:4px; margin-top:4px; }
                    .footer { text-align:center; font-size:11px; color:#555; margin-top:15px; border-top:1px dashed #ccc; padding-top:10px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>${profile.shopName}</h2>
                    <div>Owner: ${profile.ownerName}</div>
                    <div>Tel: ${profile.phoneNumber} | Email: ${profile.emailAddress}</div>
                    <div>${profile.shopAddress}, ${profile.city}</div>
                </div>
                <div class="meta">
                    <div><strong>Invoice #:</strong> ${sale.invoiceNumber}</div>
                    <div><strong>Date:</strong> $dateStr</div>
                    <div><strong>Customer:</strong> ${sale.customerName} ${if (sale.customerPhone.isNotBlank()) "(${sale.customerPhone})" else ""}</div>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Item</th>
                            <th style="text-align:center;">Qty</th>
                            <th style="text-align:right;">Price</th>
                            <th style="text-align:right;">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsHtml
                    </tbody>
                </table>
                <div class="totals">
                    <div style="display:flex; justify-content:space-between;"><span>Subtotal:</span> <span>${String.format("%.2f", sale.subtotal)} $currency</span></div>
                    ${if (sale.discount > 0) "<div style=\"display:flex; justify-content:space-between;\"><span>Discount:</span> <span>-${String.format("%.2f", sale.discount)} $currency</span></div>" else ""}
                    <div class="grand" style="display:flex; justify-content:space-between;"><span>Grand Total:</span> <span>${String.format("%.2f", sale.grandTotal)} $currency</span></div>
                    <div style="display:flex; justify-content:space-between;"><span>Paid:</span> <span>${String.format("%.2f", sale.paidAmount)} $currency</span></div>
                    <div style="display:flex; justify-content:space-between;"><span>Remaining:</span> <span>${String.format("%.2f", sale.remainingAmount)} $currency</span></div>
                </div>
                <div class="footer">
                    <div>${profile.invoiceTerms}</div>
                    <div style="margin-top:6px; font-weight:bold;">Thank you for your visit!</div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
