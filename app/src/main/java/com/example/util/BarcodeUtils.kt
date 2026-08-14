package com.example.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.EnumMap
import java.util.Random

object BarcodeUtils {

    fun generateBarcodeBitmap(
        content: String,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        width: Int = 500,
        height: Int = 180
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 1
            val bitMatrix = MultiFormatWriter().encode(content, format, width, height, hints)
            val bmWidth = bitMatrix.width
            val bmHeight = bitMatrix.height
            val bitmap = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888)

            val pixels = IntArray(bmWidth * bmHeight)
            for (y in 0 until bmHeight) {
                val offset = y * bmWidth
                for (x in 0 until bmWidth) {
                    pixels[offset + x] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }
            bitmap.setPixels(pixels, 0, bmWidth, 0, 0, bmWidth, bmHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateRandomBarcode(prefix: String): String {
        val rand = Random().nextInt(900000) + 100000
        return "$prefix-$rand"
    }

    fun generateJobCode(): String {
        val rand = Random().nextInt(9000) + 1000
        return "REP-$rand"
    }

    fun generateInvoiceNumber(): String {
        val rand = Random().nextInt(9000) + 1000
        return "INV-${System.currentTimeMillis() % 100000}-$rand"
    }
}
