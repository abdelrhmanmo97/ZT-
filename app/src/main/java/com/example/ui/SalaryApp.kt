package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavTab
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.BiometricPunchDialog
import com.example.ui.components.CorrectionRequestDialog
import com.example.ui.components.EmployeeDialog
import com.example.ui.components.ExportShareDialog
import com.example.ui.components.HrSupportDialog
import com.example.ui.components.LeaveRequestDialog
import com.example.ui.components.LoginDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ThemeColorDialog
import com.example.ui.components.TimePickerDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeavesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SalaryScreen
import com.example.ui.theme.SalaryTrackerTheme
import com.example.ui.theme.parseColorFromHex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryApp(viewModel: SalaryViewModel) {
    val state by viewModel.uiState.collectAsState()
    val employee = state.activeEmployee
    val accentHex = employee.settings.accentColor
    val accentColor = parseColorFromHex(accentHex)
    val secondaryColor = Color(0xFF22D3EE)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    SalaryTrackerTheme(
        accentColorHex = accentHex,
        isDarkTheme = state.isDarkTheme
    ) {
        // Enforce RTL Layout Direction for authentic Arabic layout
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (!state.isLoggedIn) {
                AuthScreen(
                    state = state,
                    onLogin = { email, pass, rememberMe ->
                        viewModel.loginWithEmail(email, pass, rememberMe)
                    },
                    onRegister = { name, email, code, pass, confirm, rememberMe ->
                        viewModel.registerWithEmail(name, email, code, pass, confirm, rememberMe)
                    },
                    onRequestReset = { email ->
                        viewModel.requestPasswordReset(email)
                    },
                    onCompleteReset = { email, code, newPass, confirmPass ->
                        viewModel.completePasswordReset(email, code, newPass, confirmPass)
                    },
                    onClearError = {
                        viewModel.clearAuthMessages()
                    }
                )
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Employee Avatar Badge
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(accentColor, secondaryColor)
                                                )
                                            )
                                            .clickable { viewModel.selectTab(AppNavTab.PROFILE) }
                                            .testTag("employee_avatar_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = employee.name.trim().firstOrNull()?.toString() ?: "م",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = when (state.selectedTab) {
                                                AppNavTab.HOME -> "الرئيسية"
                                                AppNavTab.ATTENDANCE -> "سجل الحضور والانصراف"
                                                AppNavTab.LEAVES -> "الإجازات والأذونات"
                                                AppNavTab.SALARY -> "مفردات الراتب والمستحقات"
                                                AppNavTab.PROFILE -> "الملف الشخصي والإعدادات"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${employee.name} • ${employee.jobTitle}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.setHrSupportDialog(true) },
                                    modifier = Modifier.testTag("hr_support_top_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpCenter,
                                        contentDescription = "الدعم الفني والـ HR",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.setThemeDialog(true) },
                                    modifier = Modifier.testTag("theme_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "المظهر والألوان",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.logout() },
                                    modifier = Modifier.testTag("top_logout_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "تسجيل الخروج",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        AppBottomNavBar(
                            selectedTab = state.selectedTab,
                            onTabSelected = { viewModel.selectTab(it) },
                            pendingLeavesCount = state.leaveRequests.count { it.status == "pending" },
                            accentColor = accentColor
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Crossfade(
                        targetState = state.selectedTab,
                        label = "tab_crossfade",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { targetTab ->
                        when (targetTab) {
                            AppNavTab.HOME -> {
                                HomeScreen(
                                    state = state,
                                    onTabSelected = { viewModel.selectTab(it) },
                                    onPunchInClicked = { viewModel.setBiometricPunchDialog(true) },
                                    onPunchOutClicked = { viewModel.punchOut() },
                                    onToggleBreakClicked = { viewModel.toggleBreak() },
                                    onRequestLeaveClicked = { viewModel.setLeaveRequestDialog(true) },
                                    onRequestCorrectionClicked = { viewModel.setCorrectionDialog(true) },
                                    accentColor = accentColor
                                )
                            }
                            AppNavTab.ATTENDANCE -> {
                                AttendanceScreen(
                                    state = state,
                                    onPunchInClicked = { viewModel.setBiometricPunchDialog(true) },
                                    onPunchOutClicked = { viewModel.punchOut() },
                                    onFillPastDaysClicked = { viewModel.fillPastDays() },
                                    onToggleBreakClicked = { viewModel.toggleBreak() },
                                    onRequestCorrectionClicked = { viewModel.setCorrectionDialog(true) },
                                    onToggleDaysList = { viewModel.toggleDaysList() },
                                    onShiftMonth = { viewModel.shiftMonth(it) },
                                    onFilterChanged = { viewModel.setFilter(it) },
                                    onQuickFillDay = { viewModel.quickFillDay(it) },
                                    onRequestTimePicker = { dateKey, isCheckIn ->
                                        viewModel.setTimePickerTarget(dateKey to if (isCheckIn) "in" else "out")
                                    },
                                    onStatusChanged = { dateKey, status -> viewModel.updateDayStatus(dateKey, status) },
                                    onMealChanged = { dateKey, meal -> viewModel.updateDayMeal(dateKey, meal) },
                                    onNoteChanged = { dateKey, note -> viewModel.updateDayNote(dateKey, note) },
                                    onClearTimes = { dateKey ->
                                        viewModel.updateDayTime(dateKey, true, null)
                                        viewModel.updateDayTime(dateKey, false, null)
                                    },
                                    onOpenLoginDialog = { viewModel.setLoginDialog(true) },
                                    accentColor = accentColor,
                                    secondaryColor = secondaryColor
                                )
                            }
                            AppNavTab.LEAVES -> {
                                LeavesScreen(
                                    state = state,
                                    onRequestLeaveClicked = { viewModel.setLeaveRequestDialog(true) },
                                    onRequestCorrectionClicked = { viewModel.setCorrectionDialog(true) },
                                    accentColor = accentColor,
                                    secondaryColor = secondaryColor
                                )
                            }
                            AppNavTab.SALARY -> {
                                SalaryScreen(
                                    state = state,
                                    onShareExportClicked = { viewModel.setShareExportDialog(true) },
                                    onShowMessage = { msg -> viewModel.showMessage(msg) },
                                    getTextSummary = { viewModel.exportTextSummary() },
                                    accentColor = accentColor,
                                    secondaryColor = secondaryColor
                                )
                            }
                            AppNavTab.PROFILE -> {
                                ProfileScreen(
                                    state = state,
                                    onOpenSettings = { viewModel.setSettingsDialog(true) },
                                    onOpenEmployeeManager = { viewModel.setEmployeeDialog(true) },
                                    onOpenThemePicker = { viewModel.setThemeDialog(true) },
                                    onOpenHrSupport = { viewModel.setHrSupportDialog(true) },
                                    onOpenBackupRestore = { viewModel.setShareExportDialog(true) },
                                    onLogout = { viewModel.logout() },
                                    onOpenLogin = { /* user is already authenticated */ }
                                )
                            }
                        }
                    }
                }
            }

            // Dialogs
            if (state.showLeaveRequestDialog) {
                LeaveRequestDialog(
                    onDismiss = { viewModel.setLeaveRequestDialog(false) },
                    onSubmit = { typeArabic, startDate, endDate, durationText, reason ->
                        viewModel.submitLeaveRequest(typeArabic, startDate, endDate, durationText, reason)
                        viewModel.setLeaveRequestDialog(false)
                    },
                    accentColor = accentColor
                )
            }

            if (state.showCorrectionDialog) {
                CorrectionRequestDialog(
                    defaultStart = employee.settings.start,
                    defaultEnd = employee.settings.end,
                    onDismiss = { viewModel.setCorrectionDialog(false) },
                    onSubmit = { dateKey, inTime, outTime, reason ->
                        viewModel.submitCorrectionRequest(dateKey, inTime, outTime, reason)
                        viewModel.setCorrectionDialog(false)
                    },
                    accentColor = accentColor
                )
            }

            if (state.showHrSupportDialog) {
                HrSupportDialog(
                    onDismiss = { viewModel.setHrSupportDialog(false) },
                    onSubmitInquiry = { inquiry ->
                        viewModel.showMessage("تم إرسال استفسارك لقسم الموارد البشرية بنجاح: $inquiry")
                        viewModel.setHrSupportDialog(false)
                    },
                    accentColor = accentColor
                )
            }

            if (state.showSettingsDialog) {
                SettingsDialog(
                    settings = employee.settings,
                    onDismiss = { viewModel.setSettingsDialog(false) },
                    onSave = { updated ->
                        viewModel.updateSettings(updated)
                        viewModel.setSettingsDialog(false)
                    },
                    accentColor = accentColor
                )
            }

            if (state.showEmployeeDialog) {
                EmployeeDialog(
                    employees = state.employees,
                    currentEmployee = state.currentEmployee,
                    onSelectEmployee = { emp ->
                        viewModel.selectEmployee(emp)
                        viewModel.setEmployeeDialog(false)
                    },
                    onAddEmployee = { name -> viewModel.addEmployee(name) },
                    onRenameEmployee = { emp, name -> viewModel.renameEmployee(emp, name) },
                    onDeleteEmployee = { emp -> viewModel.deleteEmployee(emp) },
                    onDismiss = { viewModel.setEmployeeDialog(false) },
                    accentColor = accentColor
                )
            }

            if (state.showShareExportDialog) {
                ExportShareDialog(
                    onDismiss = { viewModel.setShareExportDialog(false) },
                    getTextSummary = { viewModel.exportTextSummary() },
                    getCsv = { viewModel.exportCsv() },
                    getJson = { viewModel.exportJson() },
                    onImportJson = { json -> viewModel.importJson(json) },
                    onShowMessage = { msg -> viewModel.showMessage(msg) },
                    accentColor = accentColor
                )
            }

            if (state.showThemeDialog) {
                ThemeColorDialog(
                    currentAccentHex = accentHex,
                    isDarkTheme = state.isDarkTheme,
                    onAccentSelected = { hex -> viewModel.setAccentColor(hex) },
                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                    onDismiss = { viewModel.setThemeDialog(false) }
                )
            }

            if (state.showBiometricPunchDialog) {
                BiometricPunchDialog(
                    employeeName = employee.name,
                    onConfirmPunch = {
                        viewModel.punchIn()
                        viewModel.setBiometricPunchDialog(false)
                    },
                    onDismiss = { viewModel.setBiometricPunchDialog(false) },
                    accentColor = accentColor,
                    secondaryColor = secondaryColor
                )
            }

            if (state.showLoginDialog) {
                LoginDialog(
                    employees = state.employees,
                    currentEmployee = state.currentEmployee,
                    isLoggedIn = state.isLoggedIn,
                    onLogin = { emp ->
                        viewModel.loginEmployee(emp)
                        viewModel.setLoginDialog(false)
                    },
                    onLogout = {
                        viewModel.logout()
                        viewModel.setLoginDialog(false)
                    },
                    onDismiss = { viewModel.setLoginDialog(false) },
                    accentColor = accentColor,
                    secondaryColor = secondaryColor
                )
            }

            state.timePickerTarget?.let { (dateKey, field) ->
                val isCheckIn = field == "in"
                val rec = state.dayRecords[dateKey]
                val curTime = if (isCheckIn) rec?.inTime else rec?.outTime
                TimePickerDialog(
                    initialTime = curTime ?: if (isCheckIn) employee.settings.start else employee.settings.end,
                    title = if (isCheckIn) "تحديد وقت الحضور" else "تحديد وقت الانصراف",
                    onDismiss = { viewModel.setTimePickerTarget(null) },
                    onConfirm = { time ->
                        viewModel.updateDayTime(dateKey, isCheckIn, time)
                        viewModel.setTimePickerTarget(null)
                    },
                    onClear = {
                        viewModel.updateDayTime(dateKey, isCheckIn, null)
                        viewModel.setTimePickerTarget(null)
                    },
                    accentColor = accentColor
                )
            }
        }
    }
}
