package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.DateUtils
import kotlinx.coroutines.delay

@Composable
fun BiometricPunchDialog(
    employeeName: String,
    onConfirmPunch: () -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color,
    secondaryColor: Color = Color(0xFF22D3EE)
) {
    var isScanning by remember { mutableStateOf(true) }
    var scanSuccess by remember { mutableStateOf(false) }
    val currentTime = remember { DateUtils.currentTimeHm() }

    // Laser scanning bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_bar"
    )

    // Pulse animation for glow ring
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_ring"
    )

    LaunchedEffect(Unit) {
        // Automatically verify fingerprint after 1.4 seconds of scan animation
        delay(1300)
        isScanning = false
        scanSuccess = true
        delay(600)
        onConfirmPunch()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("biometric_punch_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بصمة تسجيل الدخول",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "الموظف: $employeeName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "الوقت الحالي: $currentTime",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Biometric Scanner Circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = if (scanSuccess) 0.35f else 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = if (scanSuccess) {
                                    listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF34D399))
                                } else {
                                    listOf(accentColor, secondaryColor, accentColor)
                                }
                            ),
                            shape = CircleShape
                        )
                        .clickable(enabled = !scanSuccess) {
                            scanSuccess = true
                            isScanning = false
                            onConfirmPunch()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (scanSuccess) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تم التحقق",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF34D399)
                        )
                    } else {
                        // Fingerprint Icon with Scanner bar
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "بصمة الإصبع",
                                modifier = Modifier.size(72.dp),
                                tint = if (isScanning) secondaryColor else accentColor
                            )
                            // Animated Laser Scanner Line
                            if (isScanning) {
                                Box(
                                    modifier = Modifier
                                        .width(74.dp)
                                        .height(3.dp)
                                        .padding(top = scanOffset.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    secondaryColor,
                                                    Color.White,
                                                    secondaryColor,
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (scanSuccess) "تم تأكيد البصمة بنجاح!" else "جارٍ مسح بصمة الحضور...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (scanSuccess) Color(0xFF34D399) else secondaryColor
                )

                Text(
                    text = if (scanSuccess) "تم تسجيل حضورك في الكشف" else "ضع إصبعك على الماسح للتأكيد الفوري",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        scanSuccess = true
                        onConfirmPunch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("confirm_punch_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (scanSuccess) Color(0xFF10B981) else accentColor
                    )
                ) {
                    Text(
                        text = if (scanSuccess) "تم التسجيل ✓" else "تأكيد البصمة الآن",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
