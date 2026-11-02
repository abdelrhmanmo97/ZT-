package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ExportShareDialog(
    onDismiss: () -> Unit,
    getTextSummary: () -> String,
    getCsv: () -> String,
    getJson: () -> String,
    onImportJson: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current
    var importJsonText by remember { mutableStateOf("") }
    var showImportSection by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .testTag("export_share_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📤 مشاركة وحفظ البيانات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Action 1: Share Summary Text
                ExportActionRow(
                    icon = Icons.Default.Share,
                    title = "مشاركة ملخص الراتب والحضور",
                    subtitle = "إرسال التقرير الشامل عبر واتساب أو تطبيقات المراسلة",
                    buttonText = "مشاركة",
                    onClick = {
                        val text = getTextSummary()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة تقرير المرتب"))
                    },
                    accentColor = accentColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Copy Text to Clipboard
                ExportActionRow(
                    icon = Icons.Default.ContentCopy,
                    title = "نسخ التقرير للحافظة",
                    subtitle = "نسخ تفاصيل الحسابات والأيام كنص منسق",
                    buttonText = "نسخ",
                    onClick = {
                        val text = getTextSummary()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("كشف المرتب", text)
                        clipboard.setPrimaryClip(clip)
                        onShowMessage("تم نسخ التقرير إلى الحافظة")
                    },
                    accentColor = accentColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 3: Share/Export CSV (Excel)
                ExportActionRow(
                    icon = Icons.Default.TableChart,
                    title = "تصدير جدول Excel / CSV",
                    subtitle = "ملف مجدول متوافق مع Excel يحتوي كافة أيام الدوام",
                    buttonText = "مشاركة CSV",
                    onClick = {
                        val csv = getCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            putExtra(Intent.EXTRA_SUBJECT, "كشف حضور ومرتب")
                            type = "text/comma-separated-values"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة ملف CSV"))
                    },
                    accentColor = accentColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 4: Backup JSON
                ExportActionRow(
                    icon = Icons.Default.Download,
                    title = "نسخة احتياطية كاملة (JSON)",
                    subtitle = "نسخ كود النسخة الاحتياطية لنقلها أو حفظها",
                    buttonText = "نسخ Backup",
                    onClick = {
                        val json = getJson()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SalaryBackupJSON", json)
                        clipboard.setPrimaryClip(clip)
                        onShowMessage("تم نسخ كود النسخة الاحتياطية (JSON) للحافظة")
                    },
                    accentColor = accentColor
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Section: Restore Backup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "استرجاع نسخة احتياطية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = accentColor
                    )
                    OutlinedButton(
                        onClick = { showImportSection = !showImportSection },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (showImportSection) "إخفاء" else "استرجاع", fontSize = 11.sp)
                    }
                }

                if (showImportSection) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الصق كود JSON الخاص بالنسخة الاحتياطية للاسترجاع:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("الصق بيانات JSON هنا...", fontSize = 11.sp) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val item = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!item.isNullOrBlank()) {
                                    importJsonText = item
                                } else {
                                    onShowMessage("الحافظة فارغة")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("لصق من الحافظة", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (importJsonText.isNotBlank()) {
                                    onImportJson(importJsonText)
                                    onDismiss()
                                } else {
                                    onShowMessage("يرجى لصق بيانات النسخة الاحتياطية")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("تأكيد الاسترجاع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
