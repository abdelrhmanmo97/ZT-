package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavTab
import com.example.ui.SalaryUiState
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun HomeScreen(
    state: SalaryUiState,
    onTabSelected: (AppNavTab) -> Unit,
    onPunchInClicked: () -> Unit,
    onPunchOutClicked: () -> Unit,
    onToggleBreakClicked: () -> Unit,
    onRequestLeaveClicked: () -> Unit,
    onRequestCorrectionClicked: () -> Unit,
    accentColor: Color,
    secondaryColor: Color = Color(0xFF22D3EE),
    modifier: Modifier = Modifier
) {
    val employee = state.currentEmployee
    val today = Calendar.getInstance()
    val todayArabicDate = "${DateUtils.getDayOfWeekArabic(today)}، ${today.get(Calendar.DAY_OF_MONTH)}/${today.get(Calendar.MONTH) + 1}/${today.get(Calendar.YEAR)}"
    val currentTime = DateUtils.currentTimeHm()
    val todayCalc = state.todayCalculation

    val isCheckedIn = !todayCalc?.inTime.isNullOrBlank()
    val isCheckedOut = !todayCalc?.outTime.isNullOrBlank()
    val isOnBreak = !todayCalc?.breakStart.isNullOrBlank() && todayCalc?.breakEnd.isNullOrBlank()

    val currentStatusText = when {
        isCheckedOut -> "انتهى الدوام اليوم (${todayCalc?.outTime})"
        isOnBreak -> "في استراحة حالياً (${todayCalc?.breakStart})"
        isCheckedIn -> "بدأ العمل الساعة ${todayCalc?.inTime}"
        else -> "لم يبدأ الدوام بعد"
    }

    val statusColor = when {
        isCheckedOut -> Color(0xFF8B93B8)
        isOnBreak -> Color(0xFFF59E0B)
        isCheckedIn -> Color(0xFF34D399)
        else -> secondaryColor
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content")
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Welcome Greeting Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "صباح الخير 👋",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "أهلاً، ${employee?.name ?: "الموظف"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${employee?.settings?.jobTitle} • ${employee?.settings?.department}",
                            fontSize = 11.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Text(
                                text = todayArabicDate,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Shift Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = currentStatusText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "دوام ${employee?.settings?.start} - ${employee?.settings?.end}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val workedMinutes = todayCalc?.actualWorkMinutes ?: 0
                    val targetMinutes = 8 * 60
                    val progress = (workedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ساعات العمل المنجزة: ${String.format(java.util.Locale.US, "%.1f", workedMinutes / 60.0)} س",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "المستهدف: 8:00 ساعات",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Circular Punch Station (بصمة الحضور والانصراف والاستراحة)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "محطة تسجيل البصمة الذكية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Punch-In
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onPunchInClicked)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(accentColor, secondaryColor))
                                    )
                                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    .testTag("home_punch_in_circle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "تسجيل حضور",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تسجيل دخول",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isCheckedIn) "✓ تم ${todayCalc?.inTime}" else "بصمة حضور",
                                fontSize = 10.sp,
                                color = if (isCheckedIn) Color(0xFF34D399) else secondaryColor
                            )
                        }

                        // Circular Break Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onToggleBreakClicked)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isOnBreak) Color(0xFFF59E0B).copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isOnBreak) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        CircleShape
                                    )
                                    .testTag("home_break_circle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = "استراحة",
                                    tint = if (isOnBreak) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isOnBreak) "إنهاء الاستراحة" else "استراحة",
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOnBreak) "جارية ☕" else "30 دقيقة",
                                fontSize = 10.sp,
                                color = if (isOnBreak) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular Punch-Out
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onPunchOutClicked)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.5.dp, Color(0xFFF87171).copy(alpha = 0.6f), CircleShape)
                                    .testTag("home_punch_out_circle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "تسجيل انصراف",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تسجيل خروج",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isCheckedOut) "✓ تم ${todayCalc?.outTime}" else "انصراف",
                                fontSize = 10.sp,
                                color = Color(0xFFF87171)
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick 4 Stat Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val workedHoursStr = String.format(java.util.Locale.US, "%.1f", (todayCalc?.actualWorkMinutes ?: 0) / 60.0)
                StatCard(
                    title = "ساعات اليوم",
                    value = "$workedHoursStr س",
                    icon = Icons.Default.Schedule,
                    tint = secondaryColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "رصيد الإجازات",
                    value = "${employee?.settings?.annualLeaveBalance ?: 14} يوم",
                    icon = Icons.Default.EventBusy,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "أيام الحضور",
                    value = "${state.dayCalculations.count { it.isRecorded }}",
                    icon = Icons.Default.Assignment,
                    tint = accentColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الإضافي",
                    value = "${String.format(java.util.Locale.US, "%.1f", state.salarySummary.overtimeHours)} س",
                    icon = Icons.Default.Paid,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Quick Services (الخدمات السريعة)
        item {
            Column {
                Text(
                    text = "الخدمات السريعة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickServiceCard(
                        title = "طلب إجازة",
                        subtitle = "سنوية أو مرضية",
                        icon = Icons.Default.EventBusy,
                        color = accentColor,
                        onClick = onRequestLeaveClicked,
                        modifier = Modifier.weight(1f)
                    )
                    QuickServiceCard(
                        title = "إذن خروج",
                        subtitle = "رصيد 4 ساعات",
                        icon = Icons.Default.AccessTime,
                        color = secondaryColor,
                        onClick = onRequestLeaveClicked,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickServiceCard(
                        title = "مفردات المرتب",
                        subtitle = "عرض والتحميل",
                        icon = Icons.Default.Paid,
                        color = Color(0xFF10B981),
                        onClick = { onTabSelected(AppNavTab.SALARY) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickServiceCard(
                        title = "تصحيح البصمة",
                        subtitle = "تسجيل المنسي",
                        icon = Icons.Default.Fingerprint,
                        color = Color(0xFFF59E0B),
                        onClick = onRequestCorrectionClicked,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. Recent Activities Feed
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "آخر الحركات والنشاطات",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isCheckedIn) {
                        ActivityRow(
                            title = "تسجيل حضور اليوم",
                            subtitle = "تم توثيق الحضور بالبصمة البيومترية",
                            time = todayCalc?.inTime ?: "",
                            isPositive = true
                        )
                    }

                    if (todayCalc?.breakStart != null) {
                        ActivityRow(
                            title = "استراحة عمل",
                            subtitle = if (todayCalc.breakEnd != null) "انتهت ${todayCalc.breakEnd}" else "استراحة نشطة حالياً",
                            time = todayCalc.breakStart,
                            isPositive = null
                        )
                    }

                    if (isCheckedOut) {
                        ActivityRow(
                            title = "تسجيل انصراف اليوم",
                            subtitle = "انتهى الدوام بنجاح",
                            time = todayCalc?.outTime ?: "",
                            isPositive = true
                        )
                    }

                    if (state.leaveRequests.isNotEmpty()) {
                        val lastReq = state.leaveRequests.first()
                        ActivityRow(
                            title = "طلب: ${lastReq.typeArabic}",
                            subtitle = "الحالة: ${when (lastReq.status) { "approved" -> "مقبول ✓"; "rejected" -> "مرفوض ✕"; else -> "قيد المراجعة ⏱" }}",
                            time = lastReq.createdAt,
                            isPositive = lastReq.status == "approved"
                        )
                    }

                    if (!isCheckedIn && state.leaveRequests.isEmpty()) {
                        Text(
                            text = "لا توجد حركات حضور مسجلة لليوم بعد. اضغط على البصمة لتسجيل الحضور.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickServiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(
    title: String,
    subtitle: String,
    time: String,
    isPositive: Boolean?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = time,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = when (isPositive) {
                true -> Color(0xFF34D399)
                false -> Color(0xFFF87171)
                else -> Color(0xFFF59E0B)
            }
        )
    }
}
