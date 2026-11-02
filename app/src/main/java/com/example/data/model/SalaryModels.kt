package com.example.data.model

enum class AppNavTab(val titleArabic: String) {
    HOME("الرئيسية"),
    ATTENDANCE("الحضور"),
    LEAVES("الإجازات"),
    SALARY("راتبي"),
    PROFILE("حسابي")
}

data class EmployeeSettings(
    val salary: Double = 6000.0,
    val divisorDays: Double = 30.0,
    val start: String = "09:00",
    val end: String = "17:00",
    val mult: Double = 1.5,
    val mealVal: Double = 20.0,
    val mealTime: String = "22:00",
    val inc: Double = 0.0,
    val loan: Double = 0.0,
    val prt: Double = 0.0,
    val delay: Double = 0.0,
    val absM: Double = 1.0,
    val d1: String = "2026-09-28",
    val d2: String = "2026-10-27",
    val holidays: List<String> = emptyList(),
    val accentColor: String = "#8B5CF6",
    val jobTitle: String = "أخصائي عمليات",
    val department: String = "الشؤون الإدارية",
    val hireDate: String = "2024-01-15",
    val annualLeaveBalance: Int = 14,
    val sickLeaveBalance: Int = 12,
    val permissionHoursBalance: Int = 4,
    val workLat: Double = 30.0444,
    val workLng: Double = 31.2357,
    val geofenceRadiusMeters: Double = 250.0,
    val geofenceEnabled: Boolean = true,
    val userRole: String = "employee" // "employee" or "admin"
)

data class Employee(
    val id: Long = 0,
    val name: String = "محمد أحمد",
    val settings: EmployeeSettings = EmployeeSettings()
) {
    val jobTitle: String get() = settings.jobTitle
    val department: String get() = settings.department
    val hireDate: String get() = settings.hireDate
    val code: String get() = if (id > 0) String.format("%04d", id) else "1024"
    val isAdmin: Boolean get() = settings.userRole == "admin"
}

data class DayRecord(
    val id: Long = 0,
    val employeeId: Long = 0,
    val dateKey: String = "",
    val inTime: String? = null,
    val outTime: String? = null,
    val breakStart: String? = null,
    val breakEnd: String? = null,
    val status: String = "", // "" -> عادي, "abs" -> غياب, "lv" -> إجازة, "ex" -> إضافي (يومين)
    val meal: String = "",   // "" -> تلقائي, "1" -> نعم, "0" -> لا
    val note: String = "",
    val punchLat: Double? = null,
    val punchLng: Double? = null,
    val withinGeofence: Boolean = true,
    val syncStatus: String = "synced" // "synced", "pending_sync"
)

data class DayCalculation(
    val dateIso: String,
    val dateKey: String,
    val dayOfMonth: Int,
    val dayOfWeek: String,
    val dayMonthFormatted: String,
    val isFriday: Boolean,
    val isHoliday: Boolean,
    val isToday: Boolean,
    val inTime: String?,
    val outTime: String?,
    val breakStart: String? = null,
    val breakEnd: String? = null,
    val status: String,
    val mealSetting: String,
    val note: String,
    val lateMinutes: Int,
    val earlyLeaveMinutes: Int = 0,
    val overtimeMinutes: Int,
    val actualWorkMinutes: Int = 0,
    val mealAwarded: Int,
    val tag: String,
    val warning: String?
) {
    val isRecorded: Boolean
        get() = !inTime.isNullOrBlank() || !outTime.isNullOrBlank() || status.isNotBlank()

    val hasOvertime: Boolean
        get() = overtimeMinutes > 0 || status == "ex"

    val isAbsentOrLeave: Boolean
        get() = status == "abs" || status == "lv"
}

data class LeaveRequest(
    val id: Long = 0,
    val employeeId: Long = 0,
    val typeArabic: String = "إجازة سنوية", // "إجازة سنوية", "إجازة مرضية", "إذن خروج"
    val startDate: String = "",
    val endDate: String = "",
    val durationText: String = "1 يوم",
    val reason: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val createdAt: String = ""
)

data class CorrectionRequest(
    val id: Long = 0,
    val employeeId: Long = 0,
    val dateKey: String = "",
    val requestedIn: String? = null,
    val requestedOut: String? = null,
    val reason: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val createdAt: String = ""
)

data class SalaryCalculation(
    val basicSalary: Double = 0.0,
    val totalLateMinutes: Int = 0,
    val totalOvertimeMinutes: Int = 0,
    val overtimeHours: Double = 0.0,
    val hourlyOvertimeRate: Double = 0.0,
    val overtimeValue: Long = 0,
    val doubleDaysCount: Int = 0,
    val doubleDaysValue: Long = 0,
    val totalMealsCount: Int = 0,
    val mealsValue: Double = 0.0,
    val productionBonus: Double = 0.0,
    val totalAdditions: Long = 0,
    val grossSalary: Long = 0,
    val absenceDaysCount: Int = 0,
    val absenceDeduction: Long = 0,
    val lateDeduction: Double = 0.0,
    val loanDeduction: Double = 0.0,
    val printDeduction: Double = 0.0,
    val totalDeductions: Long = 0,
    val netSalary: Long = 0
)

enum class AttendanceFilter(val titleArabic: String) {
    ALL("الكل"),
    RECORDED("المسجّل"),
    OVERTIME("إضافي"),
    ABSENT_LEAVE("غياب/إجازة")
}

data class AppNotification(
    val id: Long = 0,
    val employeeId: Long = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "system", // "shift", "leave", "correction", "salary", "system"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class HrTicket(
    val id: Long = 0,
    val employeeId: Long = 0,
    val employeeName: String = "",
    val category: String = "استفسار عن الراتب", // "استفسار عن الراتب", "طلب سلفة مالية", "شكوى إدارية", "اقتراح تحسين", "أخرى"
    val subject: String = "",
    val message: String = "",
    val priority: String = "عادي", // "عادي", "عاجل"
    val status: String = "pending", // "pending", "answered", "closed"
    val adminResponse: String? = null,
    val createdAt: String = "",
    val updatedAt: String? = null
)

data class LocationStatus(
    val currentLat: Double? = 30.0445,
    val currentLng: Double? = 31.2358,
    val distanceMeters: Double = 15.0,
    val isWithinGeofence: Boolean = true,
    val isSimulated: Boolean = false,
    val lastCheckedTime: String = "الآن"
)
