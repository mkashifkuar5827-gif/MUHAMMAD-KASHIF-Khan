package com.example.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SaleWithItems
import com.example.data.model.ShopProfile
import com.example.ui.components.PrintShareHelper
import com.example.ui.locale.stringRes
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceReceiptDialog(
    saleWithItems: SaleWithItems,
    profile: ShopProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sale = saleWithItems.sale
    val items = saleWithItems.items
    val currency = profile.currency
    val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(sale.date))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringRes("invoice_receipt"),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Thermal Slip Styled Box (High contrast black / off-white paper feel)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    color = Color(0xFF1E1E24),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Shop Info
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = profile.shopName.uppercase(),
                                    color = GoldPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Prop: ${profile.ownerName} • ${profile.phoneNumber}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${profile.shopAddress}, ${profile.city}",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DarkBorder)
                        }

                        // Meta details
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Invoice: ${sale.invoiceNumber}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = dateStr, color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Customer: ${sale.customerName}", color = TextSecondary, fontSize = 12.sp)
                                if (sale.customerPhone.isNotBlank()) {
                                    Text(text = "Ph: ${sale.customerPhone}", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder)
                        }

                        // Table Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "ITEM", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                Text(text = "QTY", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                                Text(text = "PRICE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "TOTAL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                            }
                        }

                        items(items) { item ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.itemName,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(text = "${item.quantity}", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                                    Text(text = String.format("%.2f", item.unitPrice), color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                    Text(text = "${String.format("%.2f", item.totalPrice)}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                }
                                if (item.barcode.isNotBlank()) {
                                    Text(text = "Code: ${item.barcode}", color = TextMuted, fontSize = 9.sp)
                                }
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DarkBorder)
                        }

                        // Totals section
                        item {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Subtotal:", color = TextSecondary, fontSize = 12.sp)
                                    Text(text = "${String.format("%.2f", sale.subtotal)} $currency", color = TextPrimary, fontSize = 12.sp)
                                }
                                if (sale.discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Discount:", color = TextSecondary, fontSize = 12.sp)
                                        Text(text = "-${String.format("%.2f", sale.discount)} $currency", color = Color(0xFFEF4444), fontSize = 12.sp)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Grand Total:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${String.format("%.2f", sale.grandTotal)} $currency", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Paid Amount:", color = TextSecondary, fontSize = 12.sp)
                                    Text(text = "${String.format("%.2f", sale.paidAmount)} $currency", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                if (sale.remainingAmount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Balance Remaining:", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${String.format("%.2f", sale.remainingAmount)} $currency", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (profile.invoiceTerms.isNotBlank()) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.invoiceTerms,
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons: Print & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val textSlip = PrintShareHelper.formatSalesReceiptText(saleWithItems, profile)
                            PrintShareHelper.shareText(context, textSlip, "Invoice #${sale.invoiceNumber}")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("share_receipt"), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val html = PrintShareHelper.generateSalesHtml(saleWithItems, profile)
                            PrintShareHelper.printHtmlReceipt(context, html, "Invoice_${sale.invoiceNumber}")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringRes("print_receipt"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
