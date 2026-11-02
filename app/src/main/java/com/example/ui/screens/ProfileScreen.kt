package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.SalaryUiState

@Composable
fun ProfileScreen(
    state: SalaryUiState,
    onOpenSettings: () -> Unit,
    onOpenEmployeeManager: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onOpenHrSupport: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onLogout: () -> Unit,
    onOpenLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emp = state.activeEmployee

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Employee Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emp.name.firstOrNull()?.toString() ?: "م",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = emp.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${emp.jobTitle} • ${emp.department}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    state.currentUser?.email?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الرقم الوظيفي: #${emp.code}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileMiniStat(
                            title = "تاريخ التعيين",
                            value = if (emp.hireDate.isBlank()) "01/01/2024" else emp.hireDate
                        )
                        ProfileMiniStat(
                            title = "نظام العمل",
                            value = "دوام كامل (8 س)"
                        )
                        ProfileMiniStat(
                            title = "حالة الحساب",
                            value = if (state.isLoggedIn) "مسجل" else "زائر",
                            valueColor = if (state.isLoggedIn) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // Account / Auth Section
        item {
            ProfileSectionHeader(title = "الأمان والدخول")
            ProfileCard {
                if (state.isLoggedIn) {
                    ProfileMenuRow(
                        icon = Icons.Default.Shield,
                        title = "تسجيل الدخول مفعل",
                        subtitle = "حسابك مؤمن برمز الحماية والبصمة",
                        onClick = {}
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "تسجيل الخروج",
                        subtitle = "قفل التطبيق وحماية بياناتك",
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = onLogout,
                        testTag = "btn_profile_logout"
                    )
                } else {
                    ProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = "تسجيل الدخول",
                        subtitle = "سجل الدخول لحماية بياناتك والوصول للميزات الإدارية",
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = onOpenLogin,
                        testTag = "btn_profile_login"
                    )
                }
            }
        }

        // System Settings Section
        item {
            ProfileSectionHeader(title = "إعدادات الدوام والنظام")
            ProfileCard {
                ProfileMenuRow(
                    icon = Icons.Default.Schedule,
                    title = "مواعيد الوردية واللوائح",
                    subtitle = "بداية الوردية 09:00 ص، مدة العمل 8 ساعات، الجمعة عطلة",
                    onClick = onOpenSettings,
                    testTag = "btn_profile_settings"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ProfileMenuRow(
                    icon = Icons.Default.ManageAccounts,
                    title = "إدارة الموظفين والتبديل",
                    subtitle = "إضافة أو تعديل موظفي الشركة",
                    onClick = onOpenEmployeeManager,
                    testTag = "btn_profile_employees"
                )
            }
        }

        // Preferences & Appearance
        item {
            ProfileSectionHeader(title = "المظهر والتخصيص")
            ProfileCard {
                ProfileMenuRow(
                    icon = Icons.Default.Palette,
                    title = "ألوان وسمات الواجهة",
                    subtitle = "اختيار النمط اللوني للتطبيق",
                    onClick = onOpenThemePicker,
                    testTag = "btn_profile_theme"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ProfileMenuRow(
                    icon = Icons.Default.CloudDownload,
                    title = "النسخ الاحتياطي والاستعادة",
                    subtitle = "حفظ نسخة من بيانات الحضور والرواتب أو استرجاعها",
                    onClick = onOpenBackupRestore,
                    testTag = "btn_profile_backup"
                )
            }
        }

        // Support & HR Help
        item {
            ProfileSectionHeader(title = "المساعدة والدعم")
            ProfileCard {
                ProfileMenuRow(
                    icon = Icons.Default.HelpCenter,
                    title = "التواصل مع الموارد البشرية (HR)",
                    subtitle = "طلب استفسار، شكوى، أو دعم فني مباشر",
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenHrSupport,
                    testTag = "btn_profile_hr_support"
                )
            }
        }

        // Version Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "نظام الموظف الذكي • Smart HR v2.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "جميع الحقوق محفوظة © 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ProfileMiniStat(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}
