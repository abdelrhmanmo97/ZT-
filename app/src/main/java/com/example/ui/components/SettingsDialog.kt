package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EmployeeSettings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    settings: EmployeeSettings,
    onDismiss: () -> Unit,
    onSave: (EmployeeSettings) -> Unit,
    accentColor: Color
) {
    var salary by remember { mutableStateOf(if (settings.salary > 0) settings.salary.toString() else "") }
    var divisorDays by remember { mutableStateOf(settings.divisorDays.toString()) }
    var startTime by remember { mutableStateOf(settings.start) }
    var endTime by remember { mutableStateOf(settings.end) }
    var overtimeMultiplier by remember { mutableStateOf(settings.mult.toString()) }
    var mealValue by remember { mutableStateOf(settings.mealVal.toString()) }
    var mealTime by remember { mutableStateOf(settings.mealTime) }
    var productionBonus by remember { mutableStateOf(if (settings.inc > 0) settings.inc.toString() else "") }
    var loanRepayment by remember { mutableStateOf(if (settings.loan > 0) settings.loan.toString() else "") }
    var personalPrint by remember { mutableStateOf(if (settings.prt > 0) settings.prt.toString() else "") }
    var delayDeduction by remember { mutableStateOf(if (settings.delay > 0) settings.delay.toString() else "") }
    var absenceMultiplier by remember { mutableStateOf(settings.absM.toString()) }
    var startDate by remember { mutableStateOf(settings.d1) }
    var endDate by remember { mutableStateOf(settings.d2) }

    var holidays by remember { mutableStateOf(settings.holidays) }
    var newHolidayInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙ الإعدادات والحسابات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 1: Financial & Shift Basics
                Text(
                    text = "بيانات الراتب ومواعيد الدوام",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingInputField(
                        label = "المرتب الأساسي",
                        value = salary,
                        onValueChange = { salary = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    SettingInputField(
                        label = "أيام الشهر للقسمة",
                        value = divisorDays,
                        onValueChange = { divisorDays = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingInputField(
                        label = "بداية الدوام",
                        value = startTime,
                        onValueChange = { startTime = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.weight(1f)
                    )
                    SettingInputField(
                        label = "نهاية الدوام",
                        value = endTime,
                        onValueChange = { endTime = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingInputField(
                        label = "معامل الإضافي (س)",
                        value = overtimeMultiplier,
                        onValueChange = { overtimeMultiplier = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    SettingInputField(
                        label = "قيمة الوجبة (ج)",
                        value = mealValue,
                        onValueChange = { mealValue = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingInputField(
                    label = "وجبة تلقائية من الساعة",
                    value = mealTime,
                    onValueChange = { mealTime = it },
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Deductions & Bonuses
                Text(
                    text = "الحوافز والاستقطاعات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingInputField(
                        label = "حوافز الإنتاج",
                        value = productionBonus,
                        onValueChange = { productionBonus = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    SettingInputField(
                        label = "سداد سلفة",
                        value = loanRepayment,
                        onValueChange = { loanRepayment = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingInputField(
                        label = "طباعة شخصية",
                        value = personalPrint,
                        onValueChange = { personalPrint = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    SettingInputField(
                        label = "خصم دقيقة التأخير",
                        value = delayDeduction,
                        onValueChange = { delayDeduction = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingInputField(
                    label = "معامل خصم الغياب",
                    value = absenceMultiplier,
                    onValueChange = { absenceMultiplier = it },
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Date Cycle
                Text(
                    text = "دورة الشهر (الفترة)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingInputField(
                        label = "أول يوم (yyyy-MM-dd)",
                        value = startDate,
                        onValueChange = { startDate = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.weight(1f)
                    )
                    SettingInputField(
                        label = "آخر يوم (yyyy-MM-dd)",
                        value = endDate,
                        onValueChange = { endDate = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Official Holidays
                Text(
                    text = "الإجازات الرسمية (تحسب بيومين عمل)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "أي يوم إجازة رسمية يتم تسجيل حضور وانصراف فيه يحسب تلقائياً بيومين عمل إضافي (زي الجمعة).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newHolidayInput,
                        onValueChange = { newHolidayInput = it },
                        placeholder = { Text("yyyy-MM-dd", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (newHolidayInput.isNotBlank() && !holidays.contains(newHolidayInput.trim())) {
                                holidays = holidays + newHolidayInput.trim()
                                newHolidayInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة", fontSize = 12.sp)
                    }
                }

                if (holidays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        holidays.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = h,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                holidays = holidays.filter { it != h }
                                            },
                                        tint = Color(0xFFF87171)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Save / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val newS = settings.copy(
                                salary = salary.toDoubleOrNull() ?: 0.0,
                                divisorDays = divisorDays.toDoubleOrNull() ?: 30.0,
                                start = startTime.trim(),
                                end = endTime.trim(),
                                mult = overtimeMultiplier.toDoubleOrNull() ?: 1.5,
                                mealVal = mealValue.toDoubleOrNull() ?: 20.0,
                                mealTime = mealTime.trim(),
                                inc = productionBonus.toDoubleOrNull() ?: 0.0,
                                loan = loanRepayment.toDoubleOrNull() ?: 0.0,
                                prt = personalPrint.toDoubleOrNull() ?: 0.0,
                                delay = delayDeduction.toDoubleOrNull() ?: 0.0,
                                absM = absenceMultiplier.toDoubleOrNull() ?: 1.0,
                                d1 = startDate.trim(),
                                d2 = endDate.trim(),
                                holidays = holidays
                            )
                            onSave(newS)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("حفظ التعديلات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}
