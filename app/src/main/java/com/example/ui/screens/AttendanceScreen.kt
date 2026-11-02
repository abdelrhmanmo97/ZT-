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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceFilter
import com.example.ui.SalaryUiState
import com.example.ui.components.DayRecordItem
import com.example.ui.components.MonthNavBar
import com.example.ui.components.QuickPunchBar

@Composable
fun AttendanceScreen(
    state: SalaryUiState,
    onPunchInClicked: () -> Unit,
    onPunchOutClicked: () -> Unit,
    onFillPastDaysClicked: () -> Unit,
    onToggleBreakClicked: () -> Unit,
    onRequestCorrectionClicked: () -> Unit,
    onToggleDaysList: () -> Unit,
    onShiftMonth: (Int) -> Unit,
    onFilterChanged: (AttendanceFilter) -> Unit,
    onQuickFillDay: (String) -> Unit,
    onRequestTimePicker: (dateKey: String, isCheckIn: Boolean) -> Unit,
    onStatusChanged: (dateKey: String, String) -> Unit,
    onMealChanged: (dateKey: String, String) -> Unit,
    onNoteChanged: (dateKey: String, String) -> Unit,
    onClearTimes: (dateKey: String) -> Unit,
    onOpenLoginDialog: () -> Unit,
    onDownloadAttendancePdf: () -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    onToggleLocationSimulation: (Boolean) -> Unit = {},
    accentColor: Color,
    secondaryColor: Color = Color(0xFF22D3EE),
    modifier: Modifier = Modifier
) {
    val employee = state.currentEmployee
    val todayCalc = state.todayCalculation

    val filteredDays = when (state.filter) {
        AttendanceFilter.ALL -> state.dayCalculations
        AttendanceFilter.RECORDED -> state.dayCalculations.filter { it.isRecorded }
        AttendanceFilter.OVERTIME -> state.dayCalculations.filter { it.hasOvertime }
        AttendanceFilter.ABSENT_LEAVE -> state.dayCalculations.filter { it.isAbsentOrLeave }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen_content")
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "01 - الحضور والانصراف الذكي",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تسجيل البصمة، تتبع الاستراحات، ومتابعة ساعات العمل وسجل الحضور",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // GPS Geofence Verification Status Card
        item {
            com.example.ui.components.GeofenceStatusCard(
                locationStatus = state.locationStatus,
                employee = employee ?: com.example.data.model.Employee(),
                onRefreshLocation = onRefreshLocation,
                onToggleSimulation = onToggleLocationSimulation,
                accentColor = accentColor
            )
        }

        // Circular Punch Console Bar
        item {
            QuickPunchBar(
                onPunchIn = onPunchInClicked,
                onPunchOut = onPunchOutClicked,
                onFillPastDays = onFillPastDaysClicked,
                onOpenLoginDialog = onOpenLoginDialog,
                isLoggedIn = state.isLoggedIn,
                loggedInEmployeeName = employee?.name,
                message = state.message,
                accentColor = accentColor,
                secondaryColor = secondaryColor
            )
        }

        // Direct Attendance Sheet PDF Download
        item {
            androidx.compose.material3.Button(
                onClick = onDownloadAttendancePdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("download_attendance_pdf_button"),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "تحميل كشف الحضور الشهري (PDF)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Today Live Metrics Card (ساعات العمل الفعلية والتأخير والانصراف المبكر)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ملخص حضور اليوم",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            label = "حضور اليوم",
                            value = todayCalc?.inTime ?: "--:--",
                            color = secondaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "انصراف اليوم",
                            value = todayCalc?.outTime ?: "--:--",
                            color = Color(0xFFF87171),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "وقت التأخير",
                            value = if ((todayCalc?.lateMinutes ?: 0) > 0) "${todayCalc?.lateMinutes} د" else "لا يوجد",
                            color = if ((todayCalc?.lateMinutes ?: 0) > 0) Color(0xFFF87171) else Color(0xFF34D399),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            label = "الانصراف المبكر",
                            value = if ((todayCalc?.earlyLeaveMinutes ?: 0) > 0) "${todayCalc?.earlyLeaveMinutes} د" else "لا يوجد",
                            color = if ((todayCalc?.earlyLeaveMinutes ?: 0) > 0) Color(0xFFF87171) else Color(0xFF34D399),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "الاستراحة",
                            value = if (!todayCalc?.breakStart.isNullOrBlank()) "${todayCalc?.breakStart}" else "لم تسجل",
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "ساعات العمل",
                            value = String.format(java.util.Locale.US, "%.1f س", (todayCalc?.actualWorkMinutes ?: 0) / 60.0),
                            color = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: Break + Correction request
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleBreakClicked,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تسجيل استراحة ☕", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onRequestCorrectionClicked,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("طلب تصحيح البصمة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Monthly Attendance Toggle Card (زر إظهار / إخفاء الأيام)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onToggleDaysList)
                    .border(
                        1.dp,
                        if (state.showDaysList) accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("attendance_toggle_days_button"),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.showDaysList) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (state.showDaysList) "إخفاء سجل الحضور الشهري" else "عرض سجل الحضور الشهري (${state.dayCalculations.size} يوم)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.showDaysList) "اضغط للطي وتبسيط الشاشة" else "اضغط لعرض تفاصيل الحضور وتعديل الأيام",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (state.showDaysList) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            }
        }

        if (state.showDaysList) {
            // Month Navigation and Filter Chips
            item {
                MonthNavBar(
                    startDate = employee?.settings?.d1 ?: "",
                    endDate = employee?.settings?.d2 ?: "",
                    onPrevMonth = { onShiftMonth(-1) },
                    onNextMonth = { onShiftMonth(1) },
                    currentFilter = state.filter,
                    onFilterChanged = onFilterChanged,
                    accentColor = accentColor
                )
            }

            // Attendance Days
            items(filteredDays, key = { it.dateKey }) { day ->
                DayRecordItem(
                    day = day,
                    accentColor = accentColor,
                    secondaryColor = secondaryColor,
                    onQuickFill = { onQuickFillDay(day.dateKey) },
                    onRequestTimePicker = { isCheckIn -> onRequestTimePicker(day.dateKey, isCheckIn) },
                    onStatusChanged = { newStatus -> onStatusChanged(day.dateKey, newStatus) },
                    onMealChanged = { newMeal -> onMealChanged(day.dateKey, newMeal) },
                    onNoteChanged = { newNote -> onNoteChanged(day.dateKey, newNote) },
                    onClearTimes = { onClearTimes(day.dateKey) }
                )
            }

            // Bottom Summary Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "الإجمالي: تأخير ${state.salarySummary.totalLateMinutes} د • إضافي ${state.salarySummary.totalOvertimeMinutes} د • وجبات ${state.salarySummary.totalMealsCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
