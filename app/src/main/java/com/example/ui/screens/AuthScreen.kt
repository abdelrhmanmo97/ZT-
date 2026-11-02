package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SalaryUiState

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    state: SalaryUiState,
    onLogin: (email: String, pass: String, rememberMe: Boolean) -> Unit,
    onRegister: (fullName: String, email: String, code: String, pass: String, confirm: String, rememberMe: Boolean) -> Unit,
    onRequestReset: (email: String) -> Unit,
    onCompleteReset: (email: String, code: String, newPass: String, confirmPass: String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    val focusManager = LocalFocusManager.current

    // Fields
    var email by remember { mutableStateOf("mohamed@company.com") }
    var password by remember { mutableStateOf("Password123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Register fields
    var fullName by remember { mutableStateOf("") }
    var employeeCode by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Forgot password fields
    var resetEmail by remember { mutableStateOf("") }
    var resetCodeInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPassword by remember { mutableStateOf("") }

    val accentViolet = MaterialTheme.colorScheme.primary
    val secondaryCyan = Color(0xFF22D3EE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1020))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo & Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accentViolet, secondaryCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "شعار نظام الموظف الذكي",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "نظام الموظف الذكي",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "سجل الحضور والانصراف وكشف المرتبات المعتمد",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Mode Tabs (تسجيل الدخول / إنشاء حساب)
            if (mode != AuthMode.FORGOT_PASSWORD) {
                TabRow(
                    selectedTabIndex = if (mode == AuthMode.LOGIN) 0 else 1,
                    containerColor = Color(0xFF141A2E),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[if (mode == AuthMode.LOGIN) 0 else 1]),
                            color = accentViolet,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = mode == AuthMode.LOGIN,
                        onClick = {
                            mode = AuthMode.LOGIN
                            onClearError()
                        },
                        text = {
                            Text(
                                text = "تسجيل الدخول",
                                fontWeight = if (mode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Normal,
                                color = if (mode == AuthMode.LOGIN) Color.White else Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier.testTag("tab_login")
                    )
                    Tab(
                        selected = mode == AuthMode.REGISTER,
                        onClick = {
                            mode = AuthMode.REGISTER
                            onClearError()
                        },
                        text = {
                            Text(
                                text = "إنشاء حساب موظف",
                                fontWeight = if (mode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Normal,
                                color = if (mode == AuthMode.REGISTER) Color.White else Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier.testTag("tab_register")
                    )
                }
            } else {
                // Header for Forgot Password Mode
                Surface(
                    color = Color(0xFF141A2E),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = secondaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "استعادة كلمة المرور",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Form Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF141A2E)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Feedback messages
                    AnimatedVisibility(
                        visible = state.authError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.authError?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .testTag("auth_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = err,
                                        color = Color(0xFFFCA5A5),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = state.authSuccess != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.authSuccess?.let { succ ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .testTag("auth_success_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = succ,
                                        color = Color(0xFF6EE7B7),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    when (mode) {
                        AuthMode.LOGIN -> {
                            // Login Form
                            Text(
                                text = "مرحباً بعودتك 👋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "سجل الدخول بالبريد الإلكتروني للوصول إلى كشف المرتب وسجل الحضور",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                            )

                            // Email input
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    onClearError()
                                },
                                label = { Text("البريد الإلكتروني (Email Address)") },
                                placeholder = { Text("example@company.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = secondaryCyan)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_email")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Password input
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    onClearError()
                                },
                                label = { Text("كلمة المرور (Password)") },
                                placeholder = { Text("أدخل كلمة المرور") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = accentViolet)
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.testTag("btn_toggle_password_visibility")
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onLogin(email, password, rememberMe)
                                    }
                                ),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_password")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Remember Me & Forgot Password Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { rememberMe = !rememberMe }
                                        .padding(vertical = 4.dp)
                                        .testTag("checkbox_remember_me_row")
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = accentViolet,
                                            uncheckedColor = Color(0xFF64748B)
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تذكرني (Remember Me)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        resetEmail = email
                                        mode = AuthMode.FORGOT_PASSWORD
                                        onClearError()
                                    },
                                    modifier = Modifier.testTag("btn_forgot_password")
                                ) {
                                    Text(
                                        text = "نسيت كلمة المرور؟",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = secondaryCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onLogin(email, password, rememberMe)
                                },
                                enabled = !state.isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_submit_login")
                            ) {
                                if (state.isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تسجيل الدخول",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Hint Box for Preconfigured Account
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = secondaryCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "بيانات الموظف الافتراضية: mohamed@company.com / Password123",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        AuthMode.REGISTER -> {
                            // Register Form
                            Text(
                                text = "اعتماد حساب موظف جديد 📋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "خاص بالموظفين المعتمدين من إدارة الموارد البشرية (HR)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                            )

                            // Full Name
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    onClearError()
                                },
                                label = { Text("الاسم الكامل للموظف") },
                                placeholder = { Text("مثال: أحمد محمود إبراهيم") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = accentViolet)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_name")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Corporate Employee Code / Passcode
                            OutlinedTextField(
                                value = employeeCode,
                                onValueChange = {
                                    employeeCode = it
                                    onClearError()
                                },
                                label = { Text("الرقم الوظيفي أو كود الاعتماد المعتمد") },
                                placeholder = { Text("مثال: 1024 أو EMP-2026") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = secondaryCyan)
                                },
                                supportingText = {
                                    Text(
                                        text = "الكود المعتمد لإنشاء الحسابات الجديدة: EMP-2026 أو 1024",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_code")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    onClearError()
                                },
                                label = { Text("البريد الإلكتروني للعمل") },
                                placeholder = { Text("user@company.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = secondaryCyan)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_email")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    onClearError()
                                },
                                label = { Text("كلمة المرور (6 خانات على الأقل)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = accentViolet)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_password")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Confirm Password
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    onClearError()
                                },
                                label = { Text("تأكيد كلمة المرور") },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = accentViolet)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onRegister(fullName, email, employeeCode, password, confirmPassword, rememberMe)
                                    }
                                ),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_confirm_password")
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Register Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onRegister(fullName, email, employeeCode, password, confirmPassword, rememberMe)
                                },
                                enabled = !state.isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_submit_register")
                            ) {
                                if (state.isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "إنشاء الحساب والاعتماد",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        AuthMode.FORGOT_PASSWORD -> {
                            // Forgot Password Form
                            Text(
                                text = "استعادة كلمة المرور 🔐",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "أدخل بريدك الإلكتروني لاستلام رمز إعادة تعيين كلمة المرور بأمان",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                            )

                            // Email input for reset
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = {
                                    resetEmail = it
                                    onClearError()
                                },
                                label = { Text("البريد الإلكتروني") },
                                placeholder = { Text("mohamed@company.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = secondaryCyan)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = outlinedFieldColors(accentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reset_email")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!state.resetCodeSent) {
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onRequestReset(resetEmail)
                                    },
                                    enabled = !state.isAuthLoading && resetEmail.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = secondaryCyan),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_send_reset_code")
                                ) {
                                    if (state.isAuthLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "إرسال رمز الاستعادة للبريد",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            } else {
                                // Step 2: Enter code & new password
                                state.simulatedResetCode?.let { simCode ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = secondaryCyan.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, secondaryCyan.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = null,
                                                tint = secondaryCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "رمز الاستعادة المرسل لبريدك: $simCode",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = resetCodeInput,
                                    onValueChange = {
                                        resetCodeInput = it
                                        onClearError()
                                    },
                                    label = { Text("رمز التحقق (6 أرقام)") },
                                    placeholder = { Text("مثال: 849201") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = secondaryCyan)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = outlinedFieldColors(accentViolet),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_reset_code")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = {
                                        newPassword = it
                                        onClearError()
                                    },
                                    label = { Text("كلمة المرور الجديدة") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = accentViolet)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                            Icon(
                                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8)
                                            )
                                        }
                                    },
                                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    colors = outlinedFieldColors(accentViolet),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_new_password")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = confirmNewPassword,
                                    onValueChange = {
                                        confirmNewPassword = it
                                        onClearError()
                                    },
                                    label = { Text("تأكيد كلمة المرور الجديدة") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = accentViolet)
                                    },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    colors = outlinedFieldColors(accentViolet),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_confirm_new_password")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onCompleteReset(resetEmail, resetCodeInput, newPassword, confirmNewPassword)
                                    },
                                    enabled = !state.isAuthLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = accentViolet),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_save_new_password")
                                ) {
                                    if (state.isAuthLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "حفظ وتعيين كلمة المرور",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Back to Login Link
                            TextButton(
                                onClick = {
                                    mode = AuthMode.LOGIN
                                    onClearError()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_back_to_login")
                            ) {
                                Text(
                                    text = "العودة إلى تسجيل الدخول",
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "نظام مشفر ومحمي وفق معايير أمان الشركات",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun outlinedFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = Color(0xFF334155),
    focusedLabelColor = accent,
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = accent,
    focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.5f),
    unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.5f)
)
