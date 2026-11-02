package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CorrectionRequest
import com.example.data.model.Employee
import com.example.data.model.HrTicket
import com.example.data.model.LeaveRequest

@Composable
fun AdminDashboardDialog(
    currentEmployee: Employee,
    allEmployees: List<Employee>,
    allLeaves: List<LeaveRequest>,
    allCorrections: List<CorrectionRequest>,
    allTickets: List<HrTicket>,
    onApproveLeave: (Long) -> Unit,
    onRejectLeave: (Long) -> Unit,
    onApproveCorrection: (Long) -> Unit,
    onRejectCorrection: (Long) -> Unit,
    onRespondToTicket: (Long, String) -> Unit,
    onUpdateWorkplaceGps: (Double, Double, Double) -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // GPS settings state
    var latText by remember { mutableStateOf(currentEmployee.settings.workLat.toString()) }
    var lngText by remember { mutableStateOf(currentEmployee.settings.workLng.toString()) }
    var radiusText by remember { mutableStateOf(currentEmployee.settings.geofenceRadiusMeters.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("admin_dashboard_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "لوحة تحكم الإدارة والموارد البشرية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "اعتماد الطلبات، إدارة التذاكر، وضبط الموقع الجغرافي",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stat Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "الموظفين",
                        count = allEmployees.size.toString(),
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "إجازات معلقة",
                        count = allLeaves.count { it.status == "pending" }.toString(),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "تصحيح بصمة",
                        count = allCorrections.count { it.status == "pending" }.toString(),
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "تذاكر HR",
                        count = allTickets.count { it.status == "pending" }.toString(),
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = accentColor,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("الإجازات (${allLeaves.count { it.status == "pending" }})", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("تصحيح البصمة (${allCorrections.count { it.status == "pending" }})", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("تذاكر الدعم (${allTickets.count { it.status == "pending" }})", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("إعدادات GPS", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content by Tab
                Box(modifier = Modifier.heightIn(max = 380.dp)) {
                    when (selectedTab) {
                        0 -> LeaveRequestsTab(allLeaves, onApproveLeave, onRejectLeave)
                        1 -> CorrectionRequestsTab(allCorrections, onApproveCorrection, onRejectCorrection)
                        2 -> HrTicketsTab(allTickets, onRespondToTicket)
                        3 -> WorkplaceGpsTab(
                            lat = latText,
                            lng = lngText,
                            radius = radiusText,
                            onLatChange = { latText = it },
                            onLngChange = { lngText = it },
                            onRadiusChange = { radiusText = it },
                            onSave = {
                                val latVal = latText.toDoubleOrNull() ?: 30.0444
                                val lngVal = lngText.toDoubleOrNull() ?: 31.2357
                                val radVal = radiusText.toDoubleOrNull() ?: 250.0
                                onUpdateWorkplaceGps(latVal, lngVal, radVal)
                            },
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, count: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LeaveRequestsTab(
    leaves: List<LeaveRequest>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (leaves.isEmpty()) {
        EmptyAdminPlaceholder("لا توجد طلبات إجازة حالياً")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leaves, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.typeArabic} (${item.durationText})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(item.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الفترة: من ${item.startDate} إلى ${item.endDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.reason.isNotBlank()) {
                            Text(text = "السبب: ${item.reason}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (item.status == "pending") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onReject(item.id) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Text("رفض", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onApprove(item.id) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("قبول واعتماد", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CorrectionRequestsTab(
    corrections: List<CorrectionRequest>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (corrections.isEmpty()) {
        EmptyAdminPlaceholder("لا توجد طلبات تصحيح بصمة حالياً")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(corrections, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تصحيح بصمة: ${item.dateKey}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(item.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المطلوب: حضور [${item.requestedIn ?: "-"}] / انصراف [${item.requestedOut ?: "-"}]",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.reason.isNotBlank()) {
                            Text(text = "السبب: ${item.reason}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (item.status == "pending") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onReject(item.id) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Text("رفض", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onApprove(item.id) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("قبول وتعديل السجل", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HrTicketsTab(
    tickets: List<HrTicket>,
    onRespond: (Long, String) -> Unit
) {
    if (tickets.isEmpty()) {
        EmptyAdminPlaceholder("لا توجد استفسارات أو شكاوى مرسلة")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tickets, key = { it.id }) { item ->
                var replyText by remember { mutableStateOf("") }
                var showReplyInput by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.employeeName} - ${item.category}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(item.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.subject, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = item.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (!item.adminResponse.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "الرد الحالي: ${item.adminResponse}",
                                    modifier = Modifier.padding(6.dp),
                                    fontSize = 10.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (showReplyInput) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("اكتب رد الإدارة للموظف...", fontSize = 10.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { showReplyInput = false }, modifier = Modifier.height(30.dp)) {
                                    Text("إلغاء", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (replyText.isNotBlank()) {
                                            onRespond(item.id, replyText)
                                            showReplyInput = false
                                        }
                                    },
                                    modifier = Modifier.height(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("إرسال الرد", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showReplyInput = true },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(30.dp)
                            ) {
                                Text(if (item.adminResponse.isNullOrBlank()) "كتابة رد للموظف" else "تعديل الرد", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkplaceGpsTab(
    lat: String,
    lng: String,
    radius: String,
    onLatChange: (String) -> Unit,
    onLngChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onSave: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "تحديد النطاق الجغرافي المسموح به لتسجيل البصمة:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lat,
            onValueChange = onLatChange,
            label = { Text("خط العرض (Latitude)", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = lng,
            onValueChange = onLngChange,
            label = { Text("خط الطول (Longitude)", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = radius,
            onValueChange = onRadiusChange,
            label = { Text("نصف قطر النطاق المسموح (بالمتر)", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Icon(imageVector = Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("حفظ إحداثيات مقر العمل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "approved", "answered" -> Pair(Color(0xFF10B981), if (status == "approved") "معتمد" else "تم الرد")
        "rejected" -> Pair(Color(0xFFEF4444), "مرفوض")
        else -> Pair(Color(0xFFF59E0B), "قيد المراجعة")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EmptyAdminPlaceholder(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 35.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
