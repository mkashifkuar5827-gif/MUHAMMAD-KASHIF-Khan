package com.example.util.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.SaleWithItems
import com.example.data.model.ShopProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-performance, formatted PDF Generator for Daily Sales & Periodic Sales Reports.
 * Generates vector-quality A4 PDF documents with custom shop branding, summary KPIs,
 * paginated transaction tables, payment breakdowns, and signature lines.
 */
object SalesReportPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 DPI)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points (72 DPI)
    private const val MARGIN = 36f // 0.5 inch margins
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    /**
     * Generates a formatted Daily Sales PDF file and saves it to the cache directory.
     */
    fun generateDailySalesPdf(
        context: Context,
        sales: List<SaleWithItems>,
        profile: ShopProfile,
        reportTitle: String = "DAILY SALES REPORT",
        periodSubtitle: String = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    ): File {
        val pdfDocument = PdfDocument()
        val currency = profile.currency

        // Calculate Totals & Summary Metrics
        val totalInvoices = sales.size
        val totalGross = sales.sumOf { it.sale.subtotal }
        val totalDiscount = sales.sumOf { it.sale.discount }
        val totalNetRevenue = sales.sumOf { it.sale.grandTotal }
        val totalPaidAmount = sales.sumOf { it.sale.paidAmount }
        val totalRemainingDue = sales.sumOf { it.sale.remainingAmount }
        val totalItemsSold = sales.sumOf { saleWithItems -> saleWithItems.items.sumOf { it.quantity } }
        val avgOrderValue = if (totalInvoices > 0) totalNetRevenue / totalInvoices else 0.0

        // Paint definitions
        val titlePaint = Paint().apply {
            color = Color.rgb(255, 255, 255)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val shopNamePaint = Paint().apply {
            color = Color.rgb(251, 191, 36) // Gold / Amber
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerLabelPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            textSize = 8.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val sectionHeadingPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Dark Slate
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPrimaryPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val textBoldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textMutedPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(255, 255, 255)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val greenTextPaint = Paint().apply {
            color = Color.rgb(22, 163, 74) // Green
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val redTextPaint = Paint().apply {
            color = Color.rgb(220, 38, 38) // Red
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bgPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            color = Color.rgb(226, 232, 240)
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeader(c: Canvas) {
            // Draw Top Brand Banner Box
            bgPaint.color = Color.rgb(15, 23, 42) // Dark Slate #0F172A
            c.drawRect(MARGIN, MARGIN, MARGIN + CONTENT_WIDTH, MARGIN + 88f, bgPaint)

            // Gold Accent Strip
            bgPaint.color = Color.rgb(217, 119, 6) // Amber #D97706
            c.drawRect(MARGIN, MARGIN + 84f, MARGIN + CONTENT_WIDTH, MARGIN + 88f, bgPaint)

            // Shop Name & Info
            c.drawText(profile.shopName.uppercase(), MARGIN + 14f, MARGIN + 24f, shopNamePaint)

            val ownerPhone = buildString {
                if (profile.ownerName.isNotBlank()) append("Owner: ${profile.ownerName}  |  ")
                if (profile.phoneNumber.isNotBlank()) append("Tel: ${profile.phoneNumber}")
            }
            c.drawText(ownerPhone, MARGIN + 14f, MARGIN + 38f, subtitlePaint)

            val addressCity = buildString {
                if (profile.shopAddress.isNotBlank()) append("${profile.shopAddress}, ")
                if (profile.city.isNotBlank()) append(profile.city)
                if (profile.emailAddress.isNotBlank()) append("  |  ${profile.emailAddress}")
            }
            c.drawText(if (addressCity.isNotBlank()) addressCity else "Mobile Sales, Parts & Hardware Repair", MARGIN + 14f, MARGIN + 51f, headerLabelPaint)

            // Report Title & Date Right-aligned
            val rightAlignX = MARGIN + CONTENT_WIDTH - 14f
            val reportTitleWidth = titlePaint.measureText(reportTitle)
            c.drawText(reportTitle, rightAlignX - reportTitleWidth, MARGIN + 28f, titlePaint)

            val dateText = "Period: $periodSubtitle"
            val dateWidth = subtitlePaint.measureText(dateText)
            c.drawText(dateText, rightAlignX - dateWidth, MARGIN + 44f, subtitlePaint)

            val genTime = "Printed: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
            val genWidth = headerLabelPaint.measureText(genTime)
            c.drawText(genTime, rightAlignX - genWidth, MARGIN + 58f, headerLabelPaint)
        }

        // Draw initial page header
        drawHeader(canvas)
        var currentY = MARGIN + 98f

        // Draw KPI Executive Cards
        val cardSpacing = 6f
        val numCards = 4
        val cardWidth = (CONTENT_WIDTH - (cardSpacing * (numCards - 1))) / numCards
        val cardHeight = 44f

        val kpiData = listOf(
            Triple("TOTAL REVENUE", String.format(Locale.getDefault(), "%.2f %s", totalNetRevenue, currency), Color.rgb(240, 253, 244)), // Light Green
            Triple("TOTAL ORDERS", "$totalInvoices Invoices ($totalItemsSold Pcs)", Color.rgb(239, 246, 255)), // Light Blue
            Triple("RECEIVED CASH", String.format(Locale.getDefault(), "%.2f %s", totalPaidAmount, currency), Color.rgb(240, 253, 250)), // Light Teal
            Triple("OUTSTANDING DUE", String.format(Locale.getDefault(), "%.2f %s", totalRemainingDue, currency), if (totalRemainingDue > 0) Color.rgb(254, 242, 242) else Color.rgb(248, 250, 252)) // Light Red
        )

        for (i in kpiData.indices) {
            val left = MARGIN + (i * (cardWidth + cardSpacing))
            val right = left + cardWidth
            val top = currentY
            val bottom = top + cardHeight

            // Card Background
            bgPaint.color = kpiData[i].third
            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, 4f, 4f, bgPaint)
            canvas.drawRoundRect(rectF, 4f, 4f, borderPaint)

            // Card Label
            textMutedPaint.textSize = 7f
            textMutedPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(kpiData[i].first, left + 8f, top + 15f, textMutedPaint)

            // Card Value
            val isDueCard = i == 3 && totalRemainingDue > 0
            val valuePaint = if (isDueCard) redTextPaint else textBoldPaint
            valuePaint.textSize = 9.5f
            canvas.drawText(kpiData[i].second, left + 8f, top + 32f, valuePaint)
        }

        currentY += cardHeight + 14f

        // Table Header Section
        canvas.drawText("SALES TRANSACTIONS BREAKDOWN", MARGIN, currentY, sectionHeadingPaint)
        currentY += 8f

        val colWidths = floatArrayOf(
            24f,  // 0: #
            56f,  // 1: Invoice #
            42f,  // 2: Time
            110f, // 3: Customer & Phone
            125f, // 4: Items & Details
            52f,  // 5: Total Amount
            54f,  // 6: Paid
            60f   // 7: Due / Status
        )

        val colTitles = arrayOf("#", "Invoice", "Time", "Customer", "Items Summary", "Total ($currency)", "Paid ($currency)", "Due ($currency)")

        fun drawTableHeaderRow(c: Canvas, y: Float) {
            val headerH = 18f
            bgPaint.color = Color.rgb(30, 41, 59) // Slate #1E293B
            c.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + headerH, bgPaint)

            var curX = MARGIN
            for (i in colTitles.indices) {
                val paddingX = 4f
                val alignRight = i in 5..7
                if (alignRight) {
                    val textW = tableHeaderPaint.measureText(colTitles[i])
                    c.drawText(colTitles[i], curX + colWidths[i] - textW - paddingX, y + 12f, tableHeaderPaint)
                } else {
                    c.drawText(colTitles[i], curX + paddingX, y + 12f, tableHeaderPaint)
                }
                curX += colWidths[i]
            }
        }

        drawTableHeaderRow(canvas, currentY)
        currentY += 18f

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val rowHeight = 22f

        if (sales.isEmpty()) {
            bgPaint.color = Color.rgb(248, 250, 252)
            canvas.drawRect(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 30f, bgPaint)
            canvas.drawRect(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 30f, borderPaint)
            textMutedPaint.textSize = 9f
            val noDataText = "No sales transactions recorded for this period."
            val textW = textMutedPaint.measureText(noDataText)
            canvas.drawText(noDataText, MARGIN + (CONTENT_WIDTH - textW) / 2f, currentY + 18f, textMutedPaint)
            currentY += 36f
        } else {
            for (index in sales.indices) {
                val item = sales[index]
                val sale = item.sale
                val itemsList = item.items

                // Check page overflow
                if (currentY + rowHeight > PAGE_HEIGHT - 65f) {
                    // Draw Footer on previous page
                    drawFooter(canvas, pageNumber, profile)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    drawHeader(canvas)
                    currentY = MARGIN + 98f
                    drawTableHeaderRow(canvas, currentY)
                    currentY += 18f
                }

                // Row Background (Zebra Striping)
                bgPaint.color = if (index % 2 == 0) Color.rgb(255, 255, 255) else Color.rgb(248, 250, 252)
                canvas.drawRect(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + rowHeight, bgPaint)

                // Row Border
                borderPaint.color = Color.rgb(241, 245, 249)
                canvas.drawLine(MARGIN, currentY + rowHeight, MARGIN + CONTENT_WIDTH, currentY + rowHeight, borderPaint)

                var curX = MARGIN
                textPrimaryPaint.textSize = 7.8f
                textBoldPaint.textSize = 7.8f

                // 0: #
                canvas.drawText("${index + 1}", curX + 4f, currentY + 14f, textMutedPaint)
                curX += colWidths[0]

                // 1: Invoice #
                canvas.drawText(sale.invoiceNumber, curX + 4f, currentY + 14f, textBoldPaint)
                curX += colWidths[1]

                // 2: Time
                val timeStr = timeFormat.format(Date(sale.date))
                canvas.drawText(timeStr, curX + 4f, currentY + 14f, textMutedPaint)
                curX += colWidths[2]

                // 3: Customer
                val customerDisplay = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer"
                val truncatedCust = if (customerDisplay.length > 20) customerDisplay.take(18) + ".." else customerDisplay
                canvas.drawText(truncatedCust, curX + 4f, currentY + 11f, textPrimaryPaint)
                if (sale.customerPhone.isNotBlank()) {
                    textMutedPaint.textSize = 6.5f
                    canvas.drawText(sale.customerPhone, curX + 4f, currentY + 19f, textMutedPaint)
                }
                curX += colWidths[3]

                // 4: Items Summary
                val itemsSummary = if (itemsList.isNotEmpty()) {
                    val summaryStr = itemsList.joinToString(", ") { "${it.quantity}x ${it.itemName}" }
                    if (summaryStr.length > 28) summaryStr.take(26) + ".." else summaryStr
                } else "N/A"
                textPrimaryPaint.textSize = 7.5f
                canvas.drawText(itemsSummary, curX + 4f, currentY + 14f, textPrimaryPaint)
                curX += colWidths[4]

                // 5: Total Amount (Right aligned)
                val totalStr = String.format(Locale.getDefault(), "%.2f", sale.grandTotal)
                val totalW = textBoldPaint.measureText(totalStr)
                canvas.drawText(totalStr, curX + colWidths[5] - totalW - 4f, currentY + 14f, textBoldPaint)
                curX += colWidths[5]

                // 6: Paid Amount (Right aligned)
                val paidStr = String.format(Locale.getDefault(), "%.2f", sale.paidAmount)
                val paidW = textPrimaryPaint.measureText(paidStr)
                canvas.drawText(paidStr, curX + colWidths[6] - paidW - 4f, currentY + 14f, textPrimaryPaint)
                curX += colWidths[6]

                // 7: Due / Status (Right aligned)
                if (sale.remainingAmount > 0) {
                    val dueStr = String.format(Locale.getDefault(), "%.2f", sale.remainingAmount)
                    redTextPaint.textSize = 7.8f
                    val dueW = redTextPaint.measureText(dueStr)
                    canvas.drawText(dueStr, curX + colWidths[7] - dueW - 4f, currentY + 14f, redTextPaint)
                } else {
                    greenTextPaint.textSize = 7.5f
                    val paidText = "PAID"
                    val paidTextW = greenTextPaint.measureText(paidText)
                    canvas.drawText(paidText, curX + colWidths[7] - paidTextW - 4f, currentY + 14f, greenTextPaint)
                }

                currentY += rowHeight
            }
        }

        // Check if totals & footer block fits, else create new page
        if (currentY + 140f > PAGE_HEIGHT - 40f) {
            drawFooter(canvas, pageNumber, profile)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawHeader(canvas)
            currentY = MARGIN + 98f
        }

        currentY += 10f

        // Draw Financial Summary Totals Box
        val summaryBoxWidth = 260f
        val summaryBoxX = MARGIN + CONTENT_WIDTH - summaryBoxWidth
        val summaryBoxH = 75f

        bgPaint.color = Color.rgb(248, 250, 252)
        val sumRect = RectF(summaryBoxX, currentY, summaryBoxX + summaryBoxWidth, currentY + summaryBoxH)
        canvas.drawRoundRect(sumRect, 4f, 4f, bgPaint)
        borderPaint.color = Color.rgb(203, 213, 225)
        canvas.drawRoundRect(sumRect, 4f, 4f, borderPaint)

        val sumLines = listOf(
            Pair("Gross Subtotal:", String.format(Locale.getDefault(), "%.2f %s", totalGross, currency)),
            Pair("Total Discounts:", String.format(Locale.getDefault(), "- %.2f %s", totalDiscount, currency)),
            Pair("Net Sales Revenue:", String.format(Locale.getDefault(), "%.2f %s", totalNetRevenue, currency)),
            Pair("Total Cash Collected:", String.format(Locale.getDefault(), "%.2f %s", totalPaidAmount, currency)),
            Pair("Total Outstanding Due:", String.format(Locale.getDefault(), "%.2f %s", totalRemainingDue, currency))
        )

        var sumY = currentY + 12f
        for ((idx, line) in sumLines.withIndex()) {
            val isNet = idx == 2
            val isDue = idx == 4 && totalRemainingDue > 0
            val p = if (isNet) textBoldPaint else textPrimaryPaint
            p.textSize = if (isNet) 8.5f else 7.5f

            canvas.drawText(line.first, summaryBoxX + 10f, sumY, p)

            val valPaint = if (isDue) redTextPaint else if (isNet) textBoldPaint else textPrimaryPaint
            valPaint.textSize = if (isNet) 8.5f else 7.5f
            val valW = valPaint.measureText(line.second)
            canvas.drawText(line.second, summaryBoxX + summaryBoxWidth - valW - 10f, sumY, valPaint)

            sumY += 13f
        }

        // Signature and Terms Area (Left side)
        val termsW = summaryBoxX - MARGIN - 16f
        textPrimaryPaint.textSize = 7.5f
        if (profile.invoiceTerms.isNotBlank()) {
            canvas.drawText("Terms & Policies:", MARGIN, currentY + 12f, textBoldPaint)
            textMutedPaint.textSize = 7f
            val termsText = if (profile.invoiceTerms.length > 90) profile.invoiceTerms.take(85) + "..." else profile.invoiceTerms
            canvas.drawText(termsText, MARGIN, currentY + 24f, textMutedPaint)
        }

        // Signature Line
        val sigY = currentY + summaryBoxH - 6f
        borderPaint.color = Color.rgb(148, 163, 184)
        canvas.drawLine(MARGIN, sigY, MARGIN + 120f, sigY, borderPaint)
        textMutedPaint.textSize = 6.5f
        canvas.drawText("Authorized Shop Signature", MARGIN, sigY + 10f, textMutedPaint)

        // Draw footer and close final page
        drawFooter(canvas, pageNumber, profile)
        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Daily_Sales_Report_$timeStamp.pdf"
        val outputFile = File(reportsDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int, profile: ShopProfile) {
        val footerY = PAGE_HEIGHT - MARGIN + 15f
        val paint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
        }

        canvas.drawLine(MARGIN, footerY - 10f, MARGIN + CONTENT_WIDTH, footerY - 10f, borderPaint)
        canvas.drawText("Kashif Mobile & Repair POS System  |  Confidential Business Report", MARGIN, footerY, paint)

        val pageStr = "Page $pageNumber"
        val pageW = paint.measureText(pageStr)
        canvas.drawText(pageStr, MARGIN + CONTENT_WIDTH - pageW, footerY, paint)
    }

    /**
     * Launches Android's native share sheet for the generated PDF report.
     */
    fun sharePdfReport(context: Context, pdfFile: File, title: String = "Daily Sales PDF Report") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Here is the sales report generated from Kashif Mobile and Repair POS.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens the generated PDF in an installed PDF viewer.
     */
    fun viewPdfReport(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(viewIntent, "Open PDF Report"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No PDF viewer app found or error opening PDF.", Toast.LENGTH_SHORT).show()
        }
    }
}
