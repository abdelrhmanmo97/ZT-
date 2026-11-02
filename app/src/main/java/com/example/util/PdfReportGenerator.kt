package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DayCalculation
import com.example.data.model.Employee
import com.example.data.model.SalaryCalculation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Generates a formal Salary Payslip (مفردات المرتب) PDF.
     */
    fun generateSalaryPayslipPdf(
        context: Context,
        employee: Employee,
        calc: SalaryCalculation,
        monthPeriod: String
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // Dark Navy
            style = Paint.Style.FILL
        }

        // Top Header Banner
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        // Accent strip
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(139, 92, 246) // Purple Accent
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 86f, 595f, 90f, accentPaint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("كشف مفردات المرتب الرسمي", 560f, 42f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("الشركة المتحدة للخدمات والعمليات - إدارة الموارد البشرية", 560f, 65f, paint)

        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawText("تاريخ الإصدار: $dateStr", 35f, 45f, paint)
        canvas.drawText("الفترة: $monthPeriod", 35f, 65f, paint)

        // Employee Info Card (Light gray container)
        var y = 115f
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val cardRect = RectF(35f, y, 560f, y + 80f)
        canvas.drawRoundRect(cardRect, 10f, 10f, cardPaint)
        canvas.drawRoundRect(cardRect, 10f, 10f, cardBorderPaint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = true

        canvas.drawText("اسم الموظف: ${employee.name}", 540f, y + 26f, paint)
        canvas.drawText("الرقم الوظيفي: ${employee.code}", 540f, y + 48f, paint)
        canvas.drawText("القسم: ${employee.department}", 540f, y + 70f, paint)

        canvas.drawText("المسمى الوظيفي: ${employee.jobTitle}", 300f, y + 26f, paint)
        canvas.drawText("الراتب الأساسي: ${employee.settings.salary.toLong()} ج.م", 300f, y + 48f, paint)
        canvas.drawText("تاريخ التعيين: ${employee.hireDate}", 300f, y + 70f, paint)

        // Financial Breakdown Sections
        y = 220f

        // Table Header
        fun drawSectionHeader(title: String, topY: Float, bgColor: Int) {
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(35f, topY, 560f, topY + 28f), 6f, 6f, bg)
            paint.color = Color.WHITE
            paint.textSize = 13f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(title, 545f, topY + 19f, paint)
        }

        fun drawTableRow(label: String, value: String, isBold: Boolean = false, isDeduction: Boolean = false) {
            paint.isFakeBoldText = isBold
            paint.textSize = 11f
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(label, 540f, y + 18f, paint)

            paint.textAlign = Paint.Align.LEFT
            paint.color = if (isDeduction) Color.rgb(220, 38, 38) else if (isBold) Color.rgb(16, 185, 129) else Color.rgb(15, 23, 42)
            canvas.drawText(value, 50f, y + 18f, paint)

            val linePaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
                strokeWidth = 1f
            }
            canvas.drawLine(35f, y + 24f, 560f, y + 24f, linePaint)
            y += 24f
        }

        // 1. المستحقات والبدلات (Earnings & Additions)
        drawSectionHeader("أولاً: المستحقات والبدلات الإضافية", y, Color.rgb(37, 99, 235))
        y += 34f

        drawTableRow("الراتب الأساسي", "${calc.basicSalary.toLong()} ج.م")
        drawTableRow("الساعات الإضافية (${String.format(Locale.US, "%.1f", calc.overtimeHours)} س - بمعدل ${calc.hourlyOvertimeRate.toLong()} ج/س)", "+ ${calc.overtimeValue} ج.م")
        drawTableRow("بدل أيام العطل والإضافي (${calc.doubleDaysCount} يوم)", "+ ${calc.doubleDaysValue} ج.م")
        drawTableRow("بدل الوجبات (${calc.totalMealsCount} وجبة)", "+ ${calc.mealsValue.toLong()} ج.م")
        drawTableRow("حافز الإنتاج والتميز", "+ ${calc.productionBonus.toLong()} ج.م")
        drawTableRow("إجمالي المستحقات (Gross Salary)", "${calc.grossSalary} ج.م", isBold = true)

        y += 15f

        // 2. الاستقطاعات والخصومات (Deductions)
        drawSectionHeader("ثانياً: الاستقطاعات والخصومات", y, Color.rgb(225, 29, 72))
        y += 34f

        drawTableRow("خصم أيام الغياب (${calc.absenceDaysCount} يوم)", "- ${calc.absenceDeduction} ج.م", isDeduction = true)
        drawTableRow("خصم دقائق التأخير (${calc.totalLateMinutes} دقيقة)", "- ${calc.lateDeduction.toLong()} ج.م", isDeduction = true)
        drawTableRow("قسط السلف والأقساط", "- ${calc.loanDeduction.toLong()} ج.م", isDeduction = true)
        drawTableRow("مصاريف الطباعة الشخصية", "- ${calc.printDeduction.toLong()} ج.م", isDeduction = true)
        drawTableRow("إجمالي الاستقطاعات", "- ${calc.totalDeductions} ج.م", isBold = true, isDeduction = true)

        y += 20f

        // 3. صافي المرتب النهائي (Net Salary Highlight Card)
        val netCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 253, 244)
            style = Paint.Style.FILL
        }
        val netCardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(34, 197, 94)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val netRect = RectF(35f, y, 560f, y + 65f)
        canvas.drawRoundRect(netRect, 10f, 10f, netCardPaint)
        canvas.drawRoundRect(netRect, 10f, 10f, netCardBorder)

        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = true
        paint.textSize = 15f
        paint.color = Color.rgb(22, 101, 52)
        canvas.drawText("صافي المرتب المستحق للصرف:", 535f, y + 38f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        paint.color = Color.rgb(21, 128, 61)
        canvas.drawText("${calc.netSalary} جنيه مصري", 55f, y + 42f, paint)

        y += 85f

        // Signatures and Footer
        val sigLinePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            strokeWidth = 1f
        }
        canvas.drawLine(50f, y + 50f, 180f, y + 50f, sigLinePaint)
        canvas.drawLine(230f, y + 50f, 360f, y + 50f, sigLinePaint)
        canvas.drawLine(410f, y + 50f, 540f, y + 50f, sigLinePaint)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.color = Color.rgb(100, 116, 139)
        paint.isFakeBoldText = false

        canvas.drawText("توقيع الموظف", 115f, y + 65f, paint)
        canvas.drawText("مسؤول الموارد البشرية", 295f, y + 65f, paint)
        canvas.drawText("اعتماد الإدارة المالية", 475f, y + 65f, paint)

        // Document Watermark / Bottom Note
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 9f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("هذا المستند معتمد وصادر إلكترونياً من تطبيق الحضور والرواتب الذكي.", 297f, 815f, paint)

        document.finishPage(page)

        // Save PDF to documents directory
        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val outputFile = File(outputDir, "payslip_${employee.code}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    /**
     * Generates a monthly Attendance Log Sheet (سجل الحضور والانصراف) PDF.
     */
    fun generateAttendanceLogPdf(
        context: Context,
        employee: Employee,
        days: List<DayCalculation>,
        monthPeriod: String
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }

        // Header
        canvas.drawRect(0f, 0f, 595f, 80f, headerPaint)
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(34, 211, 238) // Cyan accent
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 76f, 595f, 80f, accentPaint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("سجل الحضور والانصراف الشهري", 560f, 40f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("الموظف: ${employee.name} (${employee.code}) | الفترة: $monthPeriod", 560f, 62f, paint)

        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawText("تاريخ التقرير: $dateStr", 35f, 45f, paint)

        // Attendance Table Header
        var y = 105f
        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 24f), 4f, 4f, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER

        // Columns: Date (520), Day (460), In (400), Out (340), Work (280), Late (220), Overtime (160), Status/Note (85)
        canvas.drawText("التاريخ", 525f, y + 16f, paint)
        canvas.drawText("اليوم", 465f, y + 16f, paint)
        canvas.drawText("حضور", 405f, y + 16f, paint)
        canvas.drawText("انصراف", 345f, y + 16f, paint)
        canvas.drawText("ساعات العمل", 285f, y + 16f, paint)
        canvas.drawText("التأخير", 225f, y + 16f, paint)
        canvas.drawText("الإضافي", 165f, y + 16f, paint)
        canvas.drawText("الحالة والملاحظة", 85f, y + 16f, paint)

        y += 28f

        // Table Rows (take first 30 days to fit one clean page)
        val rowPaint = Paint().apply {
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        val altBg = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.8f
        }

        val displayList = days.take(28)
        for ((idx, day) in displayList.withIndex()) {
            if (idx % 2 == 1) {
                canvas.drawRect(30f, y - 4f, 565f, y + 16f, altBg)
            }

            rowPaint.color = if (day.isFriday) Color.rgb(156, 163, 175) else Color.rgb(30, 41, 59)
            rowPaint.isFakeBoldText = day.isToday

            canvas.drawText(day.dayMonthFormatted, 525f, y + 10f, rowPaint)
            canvas.drawText(day.dayOfWeek, 465f, y + 10f, rowPaint)
            canvas.drawText(day.inTime ?: "-", 405f, y + 10f, rowPaint)
            canvas.drawText(day.outTime ?: "-", 345f, y + 10f, rowPaint)

            val workHours = if (day.actualWorkMinutes > 0) "${day.actualWorkMinutes / 60}س ${day.actualWorkMinutes % 60}د" else "-"
            canvas.drawText(workHours, 285f, y + 10f, rowPaint)

            // Late
            if (day.lateMinutes > 0) {
                rowPaint.color = Color.rgb(220, 38, 38)
                canvas.drawText("${day.lateMinutes} د", 225f, y + 10f, rowPaint)
            } else {
                rowPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("-", 225f, y + 10f, rowPaint)
            }

            // Overtime
            if (day.overtimeMinutes > 0) {
                rowPaint.color = Color.rgb(16, 185, 129)
                canvas.drawText("${day.overtimeMinutes / 60}س ${day.overtimeMinutes % 60}د", 165f, y + 10f, rowPaint)
            } else {
                rowPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("-", 165f, y + 10f, rowPaint)
            }

            // Status Tag
            rowPaint.color = when (day.status) {
                "abs" -> Color.rgb(220, 38, 38)
                "lv" -> Color.rgb(217, 119, 6)
                "ex" -> Color.rgb(16, 185, 129)
                else -> Color.rgb(71, 85, 105)
            }
            canvas.drawText(day.tag.ifBlank { if (day.isFriday) "عطلة" else "حضور" }, 85f, y + 10f, rowPaint)

            canvas.drawLine(30f, y + 17f, 565f, y + 17f, linePaint)
            y += 21f
        }

        // Summary Statistics Box
        y += 10f
        val statBoxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 40f), 6f, 6f, statBoxPaint)

        val totalPresent = days.count { !it.inTime.isNullOrBlank() }
        val totalLate = days.sumOf { it.lateMinutes }
        val totalOvertime = days.sumOf { it.overtimeMinutes }
        val totalAbsence = days.count { it.status == "abs" }

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT

        canvas.drawText("إجمالي أيام الحضور: $totalPresent يوم", 545f, y + 25f, paint)
        canvas.drawText("الغياب: $totalAbsence يوم", 380f, y + 25f, paint)
        canvas.drawText("مجموع دقائق التأخير: $totalLate دقيقة", 260f, y + 25f, paint)
        canvas.drawText("إجمالي الإضافي: ${totalOvertime / 60} ساعة", 120f, y + 25f, paint)

        // Footer
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 9f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("تقرير صادر من نظام تسجيل الحضور المعتمد بالبصمة والموقع الجغرافي (GPS).", 297f, 820f, paint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val outputFile = File(outputDir, "attendance_${employee.code}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    /**
     * Shares or opens the PDF file via Android standard Intent and FileProvider.
     */
    fun openOrSharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
