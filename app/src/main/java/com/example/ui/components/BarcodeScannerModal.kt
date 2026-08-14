package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.locale.stringRes
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun BarcodeScannerModal(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualCode by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraLensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val isProcessingScan = remember { AtomicBoolean(false) }

    fun handleScanSuccess(code: String) {
        if (isProcessingScan.compareAndSet(false, true)) {
            triggerScanFeedback(context)
            onBarcodeScanned(code.trim())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.96f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = OnGoldContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringRes("scan_barcode"),
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Scan Barcode, QR Code or IMEI",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringRes("cancel"),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Camera Scanner Area
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CameraPreviewView(
                            lensFacing = cameraLensFacing,
                            torchEnabled = flashEnabled,
                            onBarcodeDetected = { code ->
                                handleScanSuccess(code)
                            }
                        )

                        // Reticle Overlay Box with Golden Corners
                        ScannerViewfinderOverlay()

                        // Camera Control Buttons Overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(30.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { flashEnabled = !flashEnabled },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flash",
                                    tint = if (flashEnabled) GoldPrimary else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                                        CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        CameraSelector.LENS_FACING_BACK
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Flip Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Camera Permission Needed",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Allow camera access to instantly scan phone IMEIs, accessories barcodes, and repair tokens.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Grant Camera Access", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Scan Preset Chips (Especially helpful for fast testing & emulator preview)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Quick Sample Codes / Test Chips:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "890123456701" to "Charger EAN",
                            "890123456703" to "Handsfree",
                            "358920112938475" to "iPhone IMEI",
                            "359812049182374" to "Samsung IMEI",
                            "KMR-1001" to "Repair #1",
                            "KMR-1002" to "Repair #2"
                        ).forEach { (code, label) ->
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.clickable {
                                    handleScanSuccess(code)
                                }
                            ) {
                                Text(
                                    text = "$label: $code",
                                    fontSize = 11.sp,
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Manual Barcode / IMEI Text Entry Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Or Enter Barcode / IMEI / Token Manually:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualCode,
                                onValueChange = { manualCode = it },
                                placeholder = {
                                    Text("e.g. 890123456701 or 358920... or KMR-1001", fontSize = 12.sp)
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (manualCode.isNotBlank()) {
                                        handleScanSuccess(manualCode.trim())
                                    }
                                },
                                enabled = manualCode.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Submit", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerViewfinderOverlay() {
    val transition = rememberInfiniteTransition(label = "scanLaser")
    val laserOffsetY by transition.animateFloat(
        initialValue = -85f,
        targetValue = 85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Target Box Reticle
        Box(
            modifier = Modifier
                .size(280.dp, 190.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, GoldPrimary.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
        ) {
            // Animated Glowing Scanning Laser Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.Center)
                    .offset(y = laserOffsetY.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GoldPrimary.copy(alpha = 0.6f),
                                GoldPrimary,
                                GoldPrimary.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Subtitle guidance under viewfinder
        Text(
            text = "Align barcode or QR code inside frame",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 125.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CameraPreviewView(
    lensFacing: Int,
    torchEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Toggle flashlight when torchEnabled changes
    LaunchedEffect(torchEnabled, activeCamera) {
        activeCamera?.cameraControl?.enableTorch(torchEnabled)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()

                    // Configure ZXing reader with all relevant barcode formats & TRY_HARDER
                    val multiFormatReader = MultiFormatReader().apply {
                        val hints = mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to listOf(
                                BarcodeFormat.QR_CODE,
                                BarcodeFormat.CODE_128,
                                BarcodeFormat.CODE_39,
                                BarcodeFormat.CODE_93,
                                BarcodeFormat.EAN_13,
                                BarcodeFormat.EAN_8,
                                BarcodeFormat.UPC_A,
                                BarcodeFormat.UPC_E,
                                BarcodeFormat.ITF,
                                BarcodeFormat.CODABAR,
                                BarcodeFormat.DATA_MATRIX,
                                BarcodeFormat.AZTEC
                            ),
                            DecodeHintType.TRY_HARDER to java.lang.Boolean.TRUE
                        )
                        setHints(hints)
                    }

                    var lastScanTimestamp = 0L

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            val now = System.currentTimeMillis()
                            if (now - lastScanTimestamp > 1000) {
                                val detectedCode = decodeBarcodeFromImageProxy(imageProxy, multiFormatReader)
                                if (!detectedCode.isNullOrBlank()) {
                                    lastScanTimestamp = now
                                    previewView.post {
                                        onBarcodeDetected(detectedCode)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("BarcodeScanner", "Analysis error", e)
                        } finally {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    activeCamera = camera
                    camera.cameraControl.enableTorch(torchEnabled)
                } catch (e: Exception) {
                    Log.e("BarcodeScanner", "Camera bind failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = {
            // No unbindAll/rebind on simple recomposition to keep 60fps fluid camera stream
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Robust ZXing decoder that handles camera image row strides and sensor rotation.
 */
private fun decodeBarcodeFromImageProxy(imageProxy: ImageProxy, reader: MultiFormatReader): String? {
    if (imageProxy.format != ImageFormat.YUV_420_888 && imageProxy.planes.isEmpty()) {
        return null
    }

    val plane = imageProxy.planes[0]
    val buffer: ByteBuffer = plane.buffer
    val rowStride = plane.rowStride
    val width = imageProxy.width
    val height = imageProxy.height
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees

    // Extract exact luminance bytes respecting rowStride
    val luminanceBytes: ByteArray
    if (rowStride == width) {
        luminanceBytes = ByteArray(buffer.remaining())
        buffer.get(luminanceBytes)
    } else {
        luminanceBytes = ByteArray(width * height)
        var outOffset = 0
        for (row in 0 until height) {
            buffer.position(row * rowStride)
            val bytesToRead = minOf(width, buffer.remaining())
            buffer.get(luminanceBytes, outOffset, bytesToRead)
            outOffset += width
        }
    }

    // Try decoding with orientation rotation
    return when (rotationDegrees) {
        90 -> {
            val rotated = rotateYUV90(luminanceBytes, width, height)
            tryDecode(rotated, height, width, reader) ?: tryDecode(luminanceBytes, width, height, reader)
        }
        270 -> {
            val rotated = rotateYUV270(luminanceBytes, width, height)
            tryDecode(rotated, height, width, reader) ?: tryDecode(luminanceBytes, width, height, reader)
        }
        180 -> {
            val rotated = rotateYUV180(luminanceBytes, width, height)
            tryDecode(rotated, width, height, reader) ?: tryDecode(luminanceBytes, width, height, reader)
        }
        else -> {
            tryDecode(luminanceBytes, width, height, reader)
        }
    }
}

private fun tryDecode(data: ByteArray, width: Int, height: Int, reader: MultiFormatReader): String? {
    return try {
        val source = PlanarYUVLuminanceSource(
            data,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result = reader.decodeWithState(binaryBitmap)
        reader.reset()
        result.text
    } catch (e: Exception) {
        reader.reset()
        null
    }
}

private fun rotateYUV90(data: ByteArray, width: Int, height: Int): ByteArray {
    val rotated = ByteArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            rotated[x * height + (height - y - 1)] = data[x + y * width]
        }
    }
    return rotated
}

private fun rotateYUV270(data: ByteArray, width: Int, height: Int): ByteArray {
    val rotated = ByteArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            rotated[(width - x - 1) * height + y] = data[x + y * width]
        }
    }
    return rotated
}

private fun rotateYUV180(data: ByteArray, width: Int, height: Int): ByteArray {
    val rotated = ByteArray(width * height)
    val total = width * height
    for (i in 0 until total) {
        rotated[total - 1 - i] = data[i]
    }
    return rotated
}

/**
 * Triggers haptic vibration and audio beep feedback on scan
 */
private fun triggerScanFeedback(context: Context) {
    try {
        // Vibrator
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(80)
        }

        // Tone Beep
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    } catch (e: Exception) {
        Log.e("BarcodeScanner", "Feedback error", e)
    }
}
