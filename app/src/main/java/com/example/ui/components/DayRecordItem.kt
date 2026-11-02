package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.DayCalculation
import com.example.ui.theme.DarkFriday
import com.example.ui.theme.WarningOrange

@Composable
fun DayRecordItem(
    day: DayCalculation,
    accentColor: Color,
    secondaryColor: Color = Color(0xFF22D3EE),
    onQuickFill: () -> Unit,
    onRequestTimePicker: (isCheckIn: Boolean) -> Unit,
    onStatusChanged: (String) -> Unit,
    onMealChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onClearTimes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var mealMenuExpanded by remember { mutableStateOf(false) }

    val isWarn = day.warning != null
    val cardBackground = when {
        day.isFriday -> DarkFriday
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        day.isToday -> secondaryColor
        isWarn -> WarningOrange
        day.isFriday -> Color(0xFF2E3B68)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val borderWidth = if (day.isToday || isWarn) 1.5.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .testTag("day_record_${day.dateKey}"),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Day Number Circle, Day Name, Date, Status Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable Day Number Avatar (Fast fills default shift)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(accentColor, secondaryColor)
                            )
                        )
                        .clickable(onClick = onQuickFill)
                        .testTag("day_circle_${day.dateKey}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day.dayOfMonth}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Day Name & Date
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = day.dayOfWeek,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (day.isToday) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(secondaryColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "اليوم",
                                    color = secondaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = day.dayMonthFormatted,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Dropdown
                Box {
                    val statusLabel = when (day.status) {
                        "abs" -> "غياب"
                        "lv" -> "إجازة"
                        "ex" -> "إضافي (يومين)"
                        else -> "عادي"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { statusMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$statusLabel ▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (day.status == "abs") Color(0xFFF87171) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("عادي") },
                            onClick = {
                                onStatusChanged("")
                                statusMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("غياب") },
                            onClick = {
                                onStatusChanged("abs")
                                statusMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("إجازة") },
                            onClick = {
                                onStatusChanged("lv")
                                statusMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("إضافي (يومين)") },
                            onClick = {
                                onStatusChanged("ex")
                                statusMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inputs Row: Check-In, Check-Out, Meal Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Check-In Box
                TimeBox(
                    label = "حضور",
                    time = day.inTime,
                    onClick = { onRequestTimePicker(true) },
                    modifier = Modifier.weight(1f)
                )

                // Check-Out Box
                TimeBox(
                    label = "انصراف",
                    time = day.outTime,
                    onClick = { onRequestTimePicker(false) },
                    modifier = Modifier.weight(1f)
                )

                // Meal Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { mealMenuExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "وجبة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (day.mealAwarded > 0) {
                                Text(
                                    text = "✓ 1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val mealLabel = when (day.mealSetting) {
                            "1" -> "نعم"
                            "0" -> "لا"
                            else -> "تلقائي"
                        }
                        Text(
                            text = mealLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = mealMenuExpanded,
                        onDismissRequest = { mealMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تلقائي (حسب وقت الانصراف)") },
                            onClick = {
                                onMealChanged("")
                                mealMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("نعم (استحقاق وجبة)") },
                            onClick = {
                                onMealChanged("1")
                                mealMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("لا (بدون وجبة)") },
                            onClick = {
                                onMealChanged("0")
                                mealMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Calculations Row: Late minutes & Overtime minutes + clear button if filled
            if (day.lateMinutes > 0 || day.overtimeMinutes > 0 || !day.inTime.isNullOrBlank() || !day.outTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (day.lateMinutes > 0) {
                            Text(
                                text = "تأخير: ${day.lateMinutes} د",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF87171)
                            )
                        }
                        if (day.overtimeMinutes > 0) {
                            Text(
                                text = "إضافي: ${day.overtimeMinutes} د",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryColor
                            )
                        }
                    }

                    if (!day.inTime.isNullOrBlank() || !day.outTime.isNullOrBlank()) {
                        Text(
                            text = "مسح المواعيد",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(onClick = onClearTimes)
                        )
                    }
                }
            }

            // Tags / Warnings
            if (day.warning != null || day.tag.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (day.warning != null) {
                        Text(
                            text = day.warning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                    }
                    if (day.tag.isNotBlank()) {
                        Text(
                            text = day.tag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Note input
            OutlinedTextField(
                value = day.note,
                onValueChange = onNoteChanged,
                placeholder = {
                    Text(
                        text = "ملاحظات...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input_${day.dateKey}"),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun TimeBox(
    label: String,
    time: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (time.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (time.isNullOrBlank()) "--:--" else time,
                    fontSize = 13.sp,
                    fontWeight = if (time.isNullOrBlank()) FontWeight.Normal else FontWeight.Bold,
                    color = if (time.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
