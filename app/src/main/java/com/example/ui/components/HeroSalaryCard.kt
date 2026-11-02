package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalaryCalculation
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HeroSalaryCard(
    calculation: SalaryCalculation,
    accentColor: Color,
    secondaryColor: Color = Color(0xFF22D3EE),
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow")
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Gradient Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(accentColor, secondaryColor)
                    )
                )
                .padding(20.dp)
                .testTag("hero_salary_card")
        ) {
            Column {
                Text(
                    text = "الصافي النهائي للمرتب",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatter.format(calculation.netSalary),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 44.sp,
                        modifier = Modifier.testTag("net_salary_text")
                    )
                    Text(
                        text = "ج.م",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Mini Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        title = "⏱ الإضافي",
                        value = "${String.format(Locale.US, "%.1f", calculation.overtimeHours)} س",
                        subValue = "+${formatter.format(calculation.overtimeValue + calculation.doubleDaysValue)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "🍴 الوجبات",
                        value = "${calculation.totalMealsCount}",
                        subValue = "+${formatter.format(calculation.mealsValue.toLong())}",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "⚠ الاستقطاع",
                        value = if (calculation.totalDeductions > 0) "-${formatter.format(calculation.totalDeductions)}" else "0",
                        subValue = "${calculation.absenceDaysCount} غياب",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expandable Detailed Breakdown Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(22.dp)
                )
                .testTag("salary_details_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل الحسابات والاستقطاعات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "توسيع",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        BreakdownRow(
                            label = "المرتب الأساسي",
                            value = "${formatter.format(calculation.basicSalary.toLong())} ج.م"
                        )
                        BreakdownRow(
                            label = "ساعات الإضافي",
                            value = "${String.format(Locale.US, "%.2f", calculation.overtimeHours)} ساعة"
                        )
                        BreakdownRow(
                            label = "سعر ساعة الإضافي",
                            value = "${String.format(Locale.US, "%.2f", calculation.hourlyOvertimeRate)} ج.م"
                        )
                        BreakdownRow(
                            label = "قيمة الإضافي",
                            value = "+${formatter.format(calculation.overtimeValue)} ج.م",
                            valueColor = Color(0xFF34D399)
                        )
                        BreakdownRow(
                            label = "أيام الإضافي (${calculation.doubleDaysCount} يوم × 2)",
                            value = "+${formatter.format(calculation.doubleDaysValue)} ج.م",
                            valueColor = Color(0xFF34D399)
                        )
                        BreakdownRow(
                            label = "بدل الوجبات (${calculation.totalMealsCount} وجبة)",
                            value = "+${formatter.format(calculation.mealsValue.toLong())} ج.م",
                            valueColor = Color(0xFF34D399)
                        )
                        if (calculation.productionBonus > 0) {
                            BreakdownRow(
                                label = "حوافز الإنتاج",
                                value = "+${formatter.format(calculation.productionBonus.toLong())} ج.م",
                                valueColor = Color(0xFF34D399)
                            )
                        }
                        BreakdownRow(
                            label = "إجمالي الإضافات",
                            value = "+${formatter.format(calculation.totalAdditions)} ج.م",
                            isBold = true,
                            valueColor = Color(0xFF34D399)
                        )
                        BreakdownRow(
                            label = "صافي المرتب قبل الخصم",
                            value = "${formatter.format(calculation.grossSalary)} ج.م",
                            isBold = true
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        BreakdownRow(
                            label = "خصم الغياب (${calculation.absenceDaysCount} يوم)",
                            value = if (calculation.absenceDeduction > 0) "-${formatter.format(calculation.absenceDeduction)} ج.م" else "0",
                            valueColor = if (calculation.absenceDeduction > 0) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BreakdownRow(
                            label = "خصم التأخير (${calculation.totalLateMinutes} دقيقة)",
                            value = if (calculation.lateDeduction > 0) "-${formatter.format(calculation.lateDeduction.toLong())} ج.م" else "0",
                            valueColor = if (calculation.lateDeduction > 0) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (calculation.loanDeduction > 0) {
                            BreakdownRow(
                                label = "سداد سلفة",
                                value = "-${formatter.format(calculation.loanDeduction.toLong())} ج.م",
                                valueColor = Color(0xFFF87171)
                            )
                        }
                        if (calculation.printDeduction > 0) {
                            BreakdownRow(
                                label = "طباعة شخصية",
                                value = "-${formatter.format(calculation.printDeduction.toLong())} ج.م",
                                valueColor = Color(0xFFF87171)
                            )
                        }
                        BreakdownRow(
                            label = "إجمالي الاستقطاعات",
                            value = if (calculation.totalDeductions > 0) "-${formatter.format(calculation.totalDeductions)} ج.م" else "0",
                            isBold = true,
                            valueColor = if (calculation.totalDeductions > 0) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        BreakdownRow(
                            label = "الصافي المستحق",
                            value = "${formatter.format(calculation.netSalary)} ج.م",
                            isBold = true,
                            fontSize = 17.sp,
                            valueColor = secondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.24f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (subValue.isNotBlank()) {
                Text(
                    text = subValue,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
