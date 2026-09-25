package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.LeeBlue
import com.example.ui.theme.LeeGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerOverlay(
    onDismiss: () -> Unit,
    onDevicePaired: (deviceName: String, browser: String, location: String) -> Unit,
    modifier: Modifier = Modifier
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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var isSimulatingScan by remember { mutableStateOf(false) }
    var detectedSession by remember { mutableStateOf<DesktopPairPayload?>(null) }
    var showPairConfirmSheet by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }

    // Animated scanning laser line
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserAnimation"
    )

    // Trigger auto-scan after 2 seconds to simulate scanning the desktop QR screen smoothly
    LaunchedEffect(hasCameraPermission, retryTrigger) {
        if (hasCameraPermission && scanError == null) {
            isSimulatingScan = true
            delay(2400)
            if (scanError == null) {
                detectedSession = DesktopPairPayload(
                    deviceName = "LeeTalk Desktop — Windows 11",
                    browser = "Electron 33.2 / Chrome 122 (x64)",
                    location = "Local Network (192.168.1.104)",
                    pairingToken = "pair_token_77a94f10"
                )
                showPairConfirmSheet = true
                isSimulatingScan = false
            }
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
            modifier = modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera Viewfinder Background Simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                )

                // Cutout overlay with targeting box
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boxSize = size.width * 0.72f
                    val left = (size.width - boxSize) / 2f
                    val top = (size.height - boxSize) / 2f - 40f

                    // Darkened vignette
                    drawRect(
                        color = Color.Black.copy(alpha = 0.65f),
                        size = size
                    )

                    // Clear center scan window
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )

                    // Border outline
                    drawRoundRect(
                        color = LeeBlue.copy(alpha = 0.8f),
                        topLeft = Offset(left, top),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 4 Corner Brackets
                    val cornerLen = 32.dp.toPx()
                    val strokeW = 5.dp.toPx()
                    val cornerColor = if (detectedSession != null) Color(0xFF10B981) else Color(0xFF38BDF8)

                    // Top-Left
                    drawLine(cornerColor, Offset(left - 2, top), Offset(left + cornerLen, top), strokeW)
                    drawLine(cornerColor, Offset(left, top - 2), Offset(left, top + cornerLen), strokeW)

                    // Top-Right
                    drawLine(cornerColor, Offset(left + boxSize + 2, top), Offset(left + boxSize - cornerLen, top), strokeW)
                    drawLine(cornerColor, Offset(left + boxSize, top - 2), Offset(left + boxSize, top + cornerLen), strokeW)

                    // Bottom-Left
                    drawLine(cornerColor, Offset(left - 2, top + boxSize), Offset(left + cornerLen, top + boxSize), strokeW)
                    drawLine(cornerColor, Offset(left, top + boxSize + 2), Offset(left, top + boxSize - cornerLen), strokeW)

                    // Bottom-Right
                    drawLine(cornerColor, Offset(left + boxSize + 2, top + boxSize), Offset(left + boxSize - cornerLen, top + boxSize), strokeW)
                    drawLine(cornerColor, Offset(left + boxSize, top + boxSize + 2), Offset(left + boxSize, top + boxSize - cornerLen), strokeW)

                    // Moving laser scanning line
                    if (scanError == null) {
                        val laserY = top + (boxSize * laserProgress)
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (detectedSession != null) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    Color.White,
                                    if (detectedSession != null) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(left + 8, laserY),
                            end = Offset(left + boxSize - 8, laserY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                // Error State Overlay over the viewfinder
                if (scanError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)),
                            shadowElevation = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.18f))
                                        .border(2.dp, Color(0xFFEF4444), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Pairing Timed Out",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = scanError ?: "The pairing connection timed out or network could not be reached.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        scanError = null
                                        isSimulatingScan = true
                                        retryTrigger++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LeeBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("qr_try_again_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Try Again", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { isFlashOn = !isFlashOn },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isFlashOn) LeeBlue else Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight",
                            tint = Color.White
                        )
                    }
                }

                // Instructions & Status Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = null,
                                    tint = LeeBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pair with LeeTalk Desktop",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Point your camera at the QR code displayed on your Windows laptop/desktop screen.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Manual simulation tap button (if user wants to instantly trigger pairing)
                    Button(
                        onClick = {
                            scanError = null
                            detectedSession = DesktopPairPayload(
                                deviceName = "LeeTalk Desktop — Windows 11",
                                browser = "Electron 33.2 / Chrome 122 (x64)",
                                location = "Local Network (192.168.1.104)",
                                pairingToken = "pair_token_77a94f10"
                            )
                            showPairConfirmSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LeeBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("pair_desktop_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detect Desktop QR Code", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary button to test error/timeout state
                    OutlinedButton(
                        onClick = {
                            isSimulatingScan = false
                            scanError = "Failed to communicate with pairing server. Network timed out (ERR_TIMED_OUT)."
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("simulate_qr_error_button")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Timeout / Failure", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Confirmation Bottom Sheet when QR Code is scanned
    if (showPairConfirmSheet && detectedSession != null) {
        val payload = detectedSession!!
        ModalBottomSheet(
            onDismissRequest = {
                showPairConfirmSheet = false
                detectedSession = null
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LeeBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = LeeBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Link New Desktop Device?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A desktop client requested access to link with your LeeTalk account.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Device: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(payload.deviceName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Client: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(payload.browser, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Location: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(payload.location, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onDevicePaired(payload.deviceName, payload.browser, payload.location)
                        showPairConfirmSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeeBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link This Device", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        showPairConfirmSheet = false
                        detectedSession = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class DesktopPairPayload(
    val deviceName: String,
    val browser: String,
    val location: String,
    val pairingToken: String
)
