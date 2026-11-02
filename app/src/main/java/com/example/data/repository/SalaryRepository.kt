package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.CorrectionRequestEntity
import com.example.data.local.DayRecordEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.LeaveRequestEntity
import com.example.data.local.SalaryDatabase
import com.example.data.model.CorrectionRequest
import com.example.data.model.DayCalculation
import com.example.data.model.DayRecord
import com.example.data.model.Employee
import com.example.data.model.EmployeeSettings
import com.example.data.model.LeaveRequest
import com.example.data.model.SalaryCalculation
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class SalaryRepository private constructor(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        SalaryDatabase::class.java,
        "salary_sheet.db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.salaryDao()
    private val sessionManager = com.example.data.local.AuthSessionManager(context.applicationContext)

    companion object {
        @Volatile
        private var instance: SalaryRepository? = null

        fun getInstance(context: Context): SalaryRepository {
            return instance ?: synchronized(this) {
                instance ?: SalaryRepository(context).also { instance = it }
            }
        }
    }

    fun getAllEmployees(): Flow<List<Employee>> {
        return dao.getAllEmployees().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getDayRecords(employeeId: Long): Flow<Map<String, DayRecord>> {
        return dao.getDayRecordsForEmployee(employeeId).map { list ->
            list.associate { it.dateKey to it.toDomain() }
        }
    }

    fun getLeaveRequests(employeeId: Long): Flow<List<LeaveRequest>> {
        return dao.getLeaveRequests(employeeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getCorrectionRequests(employeeId: Long): Flow<List<CorrectionRequest>> {
        return dao.getCorrectionRequests(employeeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun submitLeaveRequest(request: LeaveRequest): Long {
        return withContext(Dispatchers.IO) {
            dao.insertLeaveRequest(request.toEntity())
        }
    }

    suspend fun submitCorrectionRequest(request: CorrectionRequest): Long {
        return withContext(Dispatchers.IO) {
            dao.insertCorrectionRequest(request.toEntity())
        }
    }

    suspend fun getInitialOrFirstEmployee(): Employee {
        return withContext(Dispatchers.IO) {
            val list = dao.getAllEmployees().first()
            val emp = if (list.isNotEmpty()) {
                list.first().toDomain()
            } else {
                val defaults = DateUtils.getDefaultDateRange()
                val initial = EmployeeEntity(
                    name = "محمد أحمد",
                    salary = 7500.0,
                    divisorDays = 30.0,
                    startTime = "09:00",
                    endTime = "17:00",
                    overtimeMultiplier = 1.5,
                    mealValue = 25.0,
                    mealThresholdTime = "22:00",
                    productionBonus = 500.0,
                    loanRepayment = 0.0,
                    personalPrint = 0.0,
                    delayDeductionPerMin = 1.0,
                    absenceMultiplier = 1.0,
                    startDate = defaults.first,
                    endDate = defaults.second,
                    holidaysCsv = "2026-10-06",
                    accentColorHex = "#8B5CF6",
                    jobTitle = "أخصائي عمليات تشغيل",
                    department = "إدارة العمليات والخدمات",
                    hireDate = "2024-03-01",
                    annualLeaveBalance = 14,
                    sickLeaveBalance = 12,
                    permissionHoursBalance = 4
                )
                val id = dao.insertEmployee(initial)

                // Add default sample requests
                dao.insertLeaveRequest(
                    LeaveRequestEntity(
                        employeeId = id,
                        typeArabic = "إجازة سنوية",
                        startDate = "2026-10-18",
                        endDate = "2026-10-19",
                        durationText = "2 يوم",
                        reason = "ظرف عائلي طارئ",
                        status = "approved",
                        createdAt = "2026-10-15"
                    )
                )
                dao.insertLeaveRequest(
                    LeaveRequestEntity(
                        employeeId = id,
                        typeArabic = "إذن خروج",
                        startDate = "2026-10-24",
                        endDate = "2026-10-24",
                        durationText = "ساعتين",
                        reason = "مراجعة دائرة حكومية",
                        status = "pending",
                        createdAt = "2026-10-23"
                    )
                )

                dao.insertNotification(
                    com.example.data.local.AppNotificationEntity(
                        employeeId = id,
                        title = "مرحباً بك في التطبيق",
                        message = "تم تفعيل حسابك بنجاح. مواعيد العمل الرسمية من 09:00 إلى 17:00",
                        type = "system",
                        timestamp = System.currentTimeMillis() - 86400000L,
                        isRead = true
                    )
                )
                dao.insertNotification(
                    com.example.data.local.AppNotificationEntity(
                        employeeId = id,
                        title = "اعتماد كشف الراتب",
                        message = "تم اعتماد كشف حساب المرتب لهذا الشهر، يمكنك تحميله ومشاركته بصيغة PDF",
                        type = "salary",
                        timestamp = System.currentTimeMillis() - 7200000L,
                        isRead = false
                    )
                )
                dao.insertNotification(
                    com.example.data.local.AppNotificationEntity(
                        employeeId = id,
                        title = "موافقة على طلب إجازة",
                        message = "وافقت إدارة الموارد البشرية على طلب الإجازة السنوية المقدم",
                        type = "leave",
                        timestamp = System.currentTimeMillis() - 3600000L,
                        isRead = false
                    )
                )

                dao.insertTicket(
                    com.example.data.local.HrTicketEntity(
                        employeeId = id,
                        employeeName = "محمد أحمد",
                        category = "استفسار عن الراتب",
                        subject = "توضيح حساب حافز الإنتاج",
                        message = "السلام عليكم، أرجو توضيح معيار احتساب حافز الإنتاج والتميز للشهر الحالي.",
                        priority = "عادي",
                        status = "answered",
                        adminResponse = "وعليكم السلام، تم احتساب الحافز وفقاً لنسبة الحضور والالتزام بساعات العمل 100%. بالتوفيق الدائم!",
                        createdAt = DateUtils.todayIso()
                    )
                )

                initial.copy(id = id).toDomain()
            }

            // Always ensure default authorized user account is initialized
            val existingUser = dao.getUserByEmail("mohamed@company.com")
            if (existingUser == null) {
                val salt = com.example.util.PasswordSecurity.generateSalt()
                val hash = com.example.util.PasswordSecurity.hashPassword("Password123", salt)
                dao.insertUser(
                    com.example.data.local.UserAccountEntity(
                        employeeId = emp.id,
                        email = "mohamed@company.com",
                        passwordHash = hash,
                        salt = salt,
                        fullName = emp.name,
                        employeeCode = "1024",
                        createdAt = DateUtils.todayIso()
                    )
                )
            }

            emp
        }
    }

    suspend fun saveEmployee(employee: Employee): Long {
        return withContext(Dispatchers.IO) {
            if (employee.id == 0L) {
                dao.insertEmployee(employee.toEntity())
            } else {
                dao.updateEmployee(employee.toEntity())
                employee.id
            }
        }
    }

    suspend fun deleteEmployee(employee: Employee) {
        withContext(Dispatchers.IO) {
            dao.clearRecordsForEmployee(employee.id)
            dao.deleteEmployee(employee.toEntity())
        }
    }

    suspend fun saveDayRecord(
        employeeId: Long,
        dateKey: String,
        inTime: String?,
        outTime: String?,
        status: String,
        meal: String,
        note: String
    ) {
        withContext(Dispatchers.IO) {
            val records = dao.getDayRecordsForEmployeeSync(employeeId).associateBy { it.dateKey }
            val existing = records[dateKey]
            val entity = DayRecordEntity(
                id = existing?.id ?: 0,
                employeeId = employeeId,
                dateKey = dateKey,
                inTime = inTime,
                outTime = outTime,
                breakStart = existing?.breakStart,
                breakEnd = existing?.breakEnd,
                status = status,
                mealSetting = meal,
                note = note
            )
            dao.insertOrUpdateDayRecord(entity)
        }
    }

    suspend fun punchIn(
        employee: Employee,
        punchLat: Double? = null,
        punchLng: Double? = null,
        withinGeofence: Boolean = true,
        isOnline: Boolean = true
    ): String {
        return withContext(Dispatchers.IO) {
            val nowHm = DateUtils.currentTimeHm()
            val todayKey = DateUtils.todayKey()

            val dates = DateUtils.getDatesBetween(employee.settings.d1, employee.settings.d2)
            val isInRange = dates.any { DateUtils.dateToKey(it) == todayKey }
            if (!isInRange) {
                return@withContext "اليوم خارج فترة الشهر المحددة في الإعدادات"
            }

            val records = dao.getDayRecordsForEmployeeSync(employee.id).associateBy { it.dateKey }
            val existing = records[todayKey]
            val statusSync = if (isOnline) "synced" else "pending_sync"
            val updated = (existing ?: DayRecordEntity(employeeId = employee.id, dateKey = todayKey))
                .copy(
                    inTime = nowHm,
                    punchLat = punchLat,
                    punchLng = punchLng,
                    withinGeofence = withinGeofence,
                    syncStatus = statusSync
                )
            dao.insertOrUpdateDayRecord(updated)

            val geofenceText = if (withinGeofence) "داخل مقر العمل" else "خارج النطاق المعتمد"
            val offlineText = if (!isOnline) " (تم الحفظ بدون اتصال)" else ""

            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = employee.id,
                    title = "تسجيل حضور ناجح",
                    message = "تم تسجيل بصمة الحضور الساعة $nowHm ($geofenceText)$offlineText",
                    type = "shift",
                    timestamp = System.currentTimeMillis()
                )
            )

            "تم تسجيل الحضور بنجاح الساعة $nowHm ($geofenceText)$offlineText"
        }
    }

    suspend fun punchOut(
        employee: Employee,
        punchLat: Double? = null,
        punchLng: Double? = null,
        withinGeofence: Boolean = true,
        isOnline: Boolean = true
    ): String {
        return withContext(Dispatchers.IO) {
            val nowHm = DateUtils.currentTimeHm()
            val todayKey = DateUtils.todayKey()
            val yesterdayKey = DateUtils.getYesterdayKey()

            val dates = DateUtils.getDatesBetween(employee.settings.d1, employee.settings.d2)
            val records = dao.getDayRecordsForEmployeeSync(employee.id).associateBy { it.dateKey }

            val todayRec = records[todayKey]
            val yesterdayRec = records[yesterdayKey]

            val yesterdayInRange = dates.any { DateUtils.dateToKey(it) == yesterdayKey }
            val todayInRange = dates.any { DateUtils.dateToKey(it) == todayKey }
            val statusSync = if (isOnline) "synced" else "pending_sync"
            val geofenceText = if (withinGeofence) "داخل مقر العمل" else "خارج النطاق المعتمد"
            val offlineText = if (!isOnline) " (تم الحفظ بدون اتصال)" else ""

            if ((todayRec?.inTime.isNullOrBlank()) &&
                yesterdayRec != null &&
                !yesterdayRec.inTime.isNullOrBlank() &&
                yesterdayRec.outTime.isNullOrBlank() &&
                yesterdayInRange
            ) {
                dao.insertOrUpdateDayRecord(
                    yesterdayRec.copy(
                        outTime = nowHm,
                        punchLat = punchLat,
                        punchLng = punchLng,
                        withinGeofence = withinGeofence,
                        syncStatus = statusSync
                    )
                )
                dao.insertNotification(
                    com.example.data.local.AppNotificationEntity(
                        employeeId = employee.id,
                        title = "تسجيل انصراف على وردية أمس",
                        message = "تم تسجيل بصمة الانصراف الساعة $nowHm على يوم أمس ($geofenceText)",
                        type = "shift",
                        timestamp = System.currentTimeMillis()
                    )
                )
                return@withContext "تم تسجيل الانصراف الساعة $nowHm على يوم أمس"
            }

            if (!todayInRange) {
                return@withContext "اليوم خارج فترة الشهر المحددة في الإعدادات"
            }

            val updated = (todayRec ?: DayRecordEntity(employeeId = employee.id, dateKey = todayKey))
                .copy(
                    outTime = nowHm,
                    punchLat = punchLat,
                    punchLng = punchLng,
                    withinGeofence = withinGeofence,
                    syncStatus = statusSync
                )
            dao.insertOrUpdateDayRecord(updated)

            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = employee.id,
                    title = "تسجيل انصراف ناجح",
                    message = "تم تسجيل بصمة الانصراف الساعة $nowHm ($geofenceText)$offlineText",
                    type = "shift",
                    timestamp = System.currentTimeMillis()
                )
            )

            "تم تسجيل الانصراف بنجاح الساعة $nowHm ($geofenceText)$offlineText"
        }
    }

    suspend fun toggleBreak(employee: Employee): String {
        return withContext(Dispatchers.IO) {
            val nowHm = DateUtils.currentTimeHm()
            val todayKey = DateUtils.todayKey()
            val records = dao.getDayRecordsForEmployeeSync(employee.id).associateBy { it.dateKey }
            val todayRec = records[todayKey] ?: DayRecordEntity(employeeId = employee.id, dateKey = todayKey)

            if (todayRec.inTime.isNullOrBlank()) {
                return@withContext "يرجى تسجيل الحضور أولاً قبل تسجيل الاستراحة"
            }

            if (todayRec.breakStart.isNullOrBlank()) {
                dao.insertOrUpdateDayRecord(todayRec.copy(breakStart = nowHm))
                "بدأت الاستراحة الساعة $nowHm ☕"
            } else if (todayRec.breakEnd.isNullOrBlank()) {
                dao.insertOrUpdateDayRecord(todayRec.copy(breakEnd = nowHm))
                "انتهت الاستراحة وعُدت للعمل الساعة $nowHm ✓"
            } else {
                // Reset or start second break
                dao.insertOrUpdateDayRecord(todayRec.copy(breakStart = nowHm, breakEnd = null))
                "بدأت استراحة جديدة الساعة $nowHm ☕"
            }
        }
    }

    suspend fun fillPastDays(employee: Employee): Int {
        return withContext(Dispatchers.IO) {
            val dates = DateUtils.getDatesBetween(employee.settings.d1, employee.settings.d2)
            val records = dao.getDayRecordsForEmployeeSync(employee.id).associateBy { it.dateKey }
            val toInsert = mutableListOf<DayRecordEntity>()

            var count = 0
            for (cal in dates) {
                if (!DateUtils.isBeforeToday(cal) || DateUtils.isFriday(cal)) {
                    continue
                }
                val key = DateUtils.dateToKey(cal)
                val existing = records[key]
                val hasAny = existing != null && (
                        !existing.inTime.isNullOrBlank() ||
                                !existing.outTime.isNullOrBlank() ||
                                existing.status.isNotBlank()
                        )
                if (!hasAny) {
                    toInsert.add(
                        (existing ?: DayRecordEntity(employeeId = employee.id, dateKey = key)).copy(
                            inTime = employee.settings.start,
                            outTime = employee.settings.end
                        )
                    )
                    count++
                }
            }

            if (toInsert.isNotEmpty()) {
                dao.insertOrUpdateDayRecords(toInsert)
            }
            count
        }
    }

    fun calculateDays(
        employee: Employee,
        records: Map<String, DayRecord>
    ): Pair<List<DayCalculation>, SalaryCalculation> {
        val dates = DateUtils.getDatesBetween(employee.settings.d1, employee.settings.d2)
        val stMinutes = DateUtils.parseTimeToMinutes(employee.settings.start) ?: (9 * 60)
        val enMinutes = DateUtils.parseTimeToMinutes(employee.settings.end) ?: (17 * 60)
        val mealThreshold = DateUtils.parseTimeToMinutes(employee.settings.mealTime) ?: (22 * 60)
        val holidaysSet = employee.settings.holidays.toSet()

        val dayCalculations = mutableListOf<DayCalculation>()
        var totalLate = 0
        var totalOvertime = 0
        var totalMeals = 0
        var absenceCount = 0
        var doubleDaysCount = 0

        for (cal in dates) {
            val dateIso = DateUtils.calendarToIso(cal)
            val dateKey = DateUtils.dateToKey(cal)
            val dayOfWeek = DateUtils.getDayOfWeekArabic(cal)
            val dayMonth = DateUtils.getDayAndMonthFormatted(cal)
            val isFriday = DateUtils.isFriday(cal)
            val isHoliday = holidaysSet.contains(dateIso)
            val isSpecial = isFriday || isHoliday
            val isToday = DateUtils.isToday(cal)

            val rec = records[dateKey]
            val inTime = rec?.inTime
            val outTime = rec?.outTime
            val rawStatus = rec?.status ?: ""
            val note = rec?.note ?: ""

            val effectiveStatus = if (rawStatus.isNotBlank()) {
                rawStatus
            } else if (isSpecial && !inTime.isNullOrBlank() && !outTime.isNullOrBlank()) {
                "ex"
            } else {
                ""
            }

            var late = 0
            var earlyLeave = 0
            var ot = 0
            var actualWorkMin = 0
            var mealAwarded = 0
            var tag = ""

            val specialName = if (isFriday) "جمعة" else "إجازة رسمية"
            if (effectiveStatus == "ex") {
                tag = (if (isSpecial) "$specialName · " else "") + "بتتحسب بيومين"
            } else if (isSpecial) {
                tag = if (isFriday) "يوم جمعة" else "إجازة رسمية"
            }

            if (effectiveStatus == "abs") absenceCount++
            if (effectiveStatus == "ex") doubleDaysCount++

            val inMin = DateUtils.parseTimeToMinutes(inTime)
            val outMin = DateUtils.parseTimeToMinutes(outTime)

            if (inMin != null && outMin != null && (effectiveStatus.isEmpty() || effectiveStatus == "ex")) {
                var adjustedOut = outMin
                val overnight = outMin < inMin
                if (overnight) {
                    adjustedOut += 1440
                }

                actualWorkMin = maxOf(0, adjustedOut - inMin)

                if (effectiveStatus != "ex") {
                    if (inMin > stMinutes) {
                        late = inMin - stMinutes
                    }
                    if (adjustedOut < enMinutes) {
                        earlyLeave = enMinutes - adjustedOut
                    }
                    val afterOt = maxOf(0, adjustedOut - enMinutes)
                    val beforeOt = if (inMin < stMinutes) stMinutes - inMin else 0
                    ot = afterOt + beforeOt
                }

                val autoMeal = if (overnight || adjustedOut >= mealThreshold) 1 else 0
                mealAwarded = when (rec?.meal) {
                    "1" -> 1
                    "0" -> 0
                    else -> autoMeal
                }
            } else if (rec?.meal == "1") {
                mealAwarded = 1
            }

            val warn = if (!isToday && effectiveStatus.isEmpty()) {
                if (!inTime.isNullOrBlank() && outTime.isNullOrBlank()) {
                    "⚠ ناقص الانصراف"
                } else if (inTime.isNullOrBlank() && !outTime.isNullOrBlank()) {
                    "⚠ ناقص الحضور"
                } else null
            } else null

            totalLate += late
            totalOvertime += ot
            totalMeals += mealAwarded

            dayCalculations.add(
                DayCalculation(
                    dateIso = dateIso,
                    dateKey = dateKey,
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    dayOfWeek = dayOfWeek,
                    dayMonthFormatted = dayMonth,
                    isFriday = isFriday,
                    isHoliday = isHoliday,
                    isToday = isToday,
                    inTime = inTime,
                    outTime = outTime,
                    breakStart = rec?.breakStart,
                    breakEnd = rec?.breakEnd,
                    status = effectiveStatus,
                    mealSetting = rec?.meal ?: "",
                    note = note,
                    lateMinutes = late,
                    earlyLeaveMinutes = earlyLeave,
                    overtimeMinutes = ot,
                    actualWorkMinutes = actualWorkMin,
                    mealAwarded = mealAwarded,
                    tag = tag,
                    warning = warn
                )
            )
        }

        val s = employee.settings
        val divisor = if (s.divisorDays > 0) s.divisorDays else 30.0
        val standardShiftHours = ((enMinutes - stMinutes) / 60.0).let { if (it > 0) it else 8.0 }

        val overtimeHours = totalOvertime / 60.0
        val hourlyRate = (s.salary / divisor) / standardShiftHours * s.mult
        val overtimeVal = Math.round(overtimeHours * hourlyRate)
        val mealVal = totalMeals * s.mealVal
        val doubleDaysVal = Math.round(doubleDaysCount * 2.0 * s.salary / divisor)
        val additions = overtimeVal + Math.round(mealVal) + doubleDaysVal + Math.round(s.inc)
        val gross = Math.round(s.salary) + additions

        val absenceDed = Math.round(absenceCount * (s.salary / divisor) * s.absM)
        val delayDed = totalLate * s.delay
        val deductions = absenceDed + Math.round(delayDed) + Math.round(s.loan) + Math.round(s.prt)
        val net = gross - deductions

        val salarySummary = SalaryCalculation(
            basicSalary = s.salary,
            totalLateMinutes = totalLate,
            totalOvertimeMinutes = totalOvertime,
            overtimeHours = overtimeHours,
            hourlyOvertimeRate = hourlyRate,
            overtimeValue = overtimeVal,
            doubleDaysCount = doubleDaysCount,
            doubleDaysValue = doubleDaysVal,
            totalMealsCount = totalMeals,
            mealsValue = mealVal,
            productionBonus = s.inc,
            totalAdditions = additions,
            grossSalary = gross,
            absenceDaysCount = absenceCount,
            absenceDeduction = absenceDed,
            lateDeduction = delayDed,
            loanDeduction = s.loan,
            printDeduction = s.prt,
            totalDeductions = deductions,
            netSalary = net
        )

        return Pair(dayCalculations, salarySummary)
    }

    fun exportToJson(employee: Employee, records: Map<String, DayRecord>): String {
        val root = JSONObject()
        root.put("name", employee.name)
        val s = employee.settings
        val sObj = JSONObject().apply {
            put("salary", s.salary)
            put("days", s.divisorDays)
            put("start", s.start)
            put("end", s.end)
            put("mult", s.mult)
            put("mealVal", s.mealVal)
            put("mealTime", s.mealTime)
            put("inc", s.inc)
            put("loan", s.loan)
            put("prt", s.prt)
            put("delay", s.delay)
            put("absM", s.absM)
            put("d1", s.d1)
            put("d2", s.d2)
            put("hol", JSONArray(s.holidays))
            put("color", s.accentColor)
            put("jobTitle", s.jobTitle)
            put("department", s.department)
        }
        root.put("settings", sObj)

        val daysObj = JSONObject()
        for ((k, v) in records) {
            val d = JSONObject()
            v.inTime?.let { d.put("in", it) }
            v.outTime?.let { d.put("out", it) }
            v.breakStart?.let { d.put("bIn", it) }
            v.breakEnd?.let { d.put("bOut", it) }
            if (v.status.isNotBlank()) d.put("st", v.status)
            if (v.meal.isNotBlank()) d.put("meal", v.meal)
            if (v.note.isNotBlank()) d.put("note", v.note)
            daysObj.put(k, d)
        }
        root.put("days", daysObj)

        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String, currentEmployee: Employee): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val obj = JSONObject(jsonString)
                val name = obj.optString("name", currentEmployee.name)
                val sObj = obj.optJSONObject("settings")
                val s = currentEmployee.settings

                val holidaysList = mutableListOf<String>()
                sObj?.optJSONArray("hol")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        holidaysList.add(arr.getString(i))
                    }
                }

                val updatedSettings = if (sObj != null) {
                    s.copy(
                        salary = sObj.optDouble("salary", s.salary),
                        divisorDays = sObj.optDouble("days", s.divisorDays),
                        start = sObj.optString("start", s.start),
                        end = sObj.optString("end", s.end),
                        mult = sObj.optDouble("mult", s.mult),
                        mealVal = sObj.optDouble("mealVal", s.mealVal),
                        mealTime = sObj.optString("mealTime", s.mealTime),
                        inc = sObj.optDouble("inc", s.inc),
                        loan = sObj.optDouble("loan", s.loan),
                        prt = sObj.optDouble("prt", s.prt),
                        delay = sObj.optDouble("delay", s.delay),
                        absM = sObj.optDouble("absM", s.absM),
                        d1 = sObj.optString("d1", s.d1),
                        d2 = sObj.optString("d2", s.d2),
                        holidays = holidaysList,
                        accentColor = sObj.optString("color", s.accentColor),
                        jobTitle = sObj.optString("jobTitle", s.jobTitle),
                        department = sObj.optString("department", s.department)
                    )
                } else s

                val updatedEmployee = currentEmployee.copy(
                    name = name,
                    settings = updatedSettings
                )
                dao.updateEmployee(updatedEmployee.toEntity())

                val daysObj = obj.optJSONObject("days")
                if (daysObj != null) {
                    val list = mutableListOf<DayRecordEntity>()
                    val keys = daysObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val d = daysObj.getJSONObject(key)
                        list.add(
                            DayRecordEntity(
                                employeeId = currentEmployee.id,
                                dateKey = key,
                                inTime = if (d.has("in")) d.getString("in") else null,
                                outTime = if (d.has("out")) d.getString("out") else null,
                                breakStart = if (d.has("bIn")) d.getString("bIn") else null,
                                breakEnd = if (d.has("bOut")) d.getString("bOut") else null,
                                status = d.optString("st", ""),
                                mealSetting = d.optString("meal", ""),
                                note = d.optString("note", "")
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        dao.insertOrUpdateDayRecords(list)
                    }
                }
                Result.success("تم استرجاع البيانات بنجاح")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun buildSummaryText(
        employee: Employee,
        days: List<DayCalculation>,
        calc: SalaryCalculation
    ): String {
        val sb = StringBuilder()
        sb.append("📋 مفردات المرتب وكشف الحضور الشهري\n")
        sb.append("الموظف: ${employee.name} (${employee.settings.jobTitle})\n")
        sb.append("القسم: ${employee.settings.department}\n")
        sb.append("الفترة: ${employee.settings.d1} إلى ${employee.settings.d2}\n")
        sb.append("====================================\n")
        sb.append("💵 صافي المرتب المستحق: ${calc.netSalary} ج.م\n\n")
        sb.append("1. تفاصيل الاستحقاقات والبدلات:\n")
        sb.append("• المرتب الأساسي: ${calc.basicSalary.toLong()} ج.م\n")
        sb.append("• ساعات الإضافي (${String.format(java.util.Locale.US, "%.1f", calc.overtimeHours)} ساعة): +${calc.overtimeValue} ج.م\n")
        sb.append("• أيام إضافي (${calc.doubleDaysCount} يوم مضاعف): +${calc.doubleDaysValue} ج.م\n")
        sb.append("• بدل الوجبات (${calc.totalMealsCount} وجبة): +${calc.mealsValue.toLong()} ج.م\n")
        if (calc.productionBonus > 0) {
            sb.append("• حوافز ومكافآت الإنتاج: +${calc.productionBonus.toLong()} ج.م\n")
        }
        sb.append("• إجمالي الإضافات: +${calc.totalAdditions} ج.م\n")
        sb.append("• إجمالي المرتب قبل الخصومات: ${calc.grossSalary} ج.م\n\n")
        sb.append("2. الاستقطاعات والخصومات:\n")
        sb.append("• خصم الغياب (${calc.absenceDaysCount} يوم): -${calc.absenceDeduction} ج.م\n")
        sb.append("• خصم التأخير والانصراف المبكر (${calc.totalLateMinutes} دقيقة): -${calc.lateDeduction.toLong()} ج.م\n")
        if (calc.loanDeduction > 0) sb.append("• سداد سلفة / أقساط: -${calc.loanDeduction.toLong()} ج.م\n")
        if (calc.printDeduction > 0) sb.append("• استقطاعات أخرى: -${calc.printDeduction.toLong()} ج.م\n")
        sb.append("• إجمالي الاستقطاعات: -${calc.totalDeductions} ج.م\n")
        sb.append("====================================\n")
        sb.append("الصافي النهائي المحول للبنك: ${calc.netSalary} ج.م\n")
        return sb.toString()
    }

    fun buildCsv(
        employee: Employee,
        days: List<DayCalculation>,
        calc: SalaryCalculation
    ): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")
        sb.append("\"كشف الحضور والمرتب: ${employee.name}\",\"${employee.settings.d1} إلى ${employee.settings.d2}\"\n\n")
        sb.append("\"اليوم\",\"التاريخ\",\"حضور\",\"انصراف\",\"استراحة\",\"ساعات العمل\",\"الحالة\",\"تأخير\",\"إضافي\",\"وجبة\",\"ملاحظات\"\n")

        for (d in days) {
            val statusLabel = when (d.status) {
                "abs" -> "غياب"
                "lv" -> "إجازة"
                "ex" -> "إضافي (يومين)"
                else -> "عادي"
            }
            val breakStr = if (!d.breakStart.isNullOrBlank()) "${d.breakStart}-${d.breakEnd ?: ""}" else ""
            val workHours = if (d.actualWorkMinutes > 0) String.format(java.util.Locale.US, "%.1f س", d.actualWorkMinutes / 60.0) else ""

            sb.append("\"${d.dayOfWeek}\",\"${d.dayMonthFormatted}\",\"${d.inTime ?: ""}\",\"${d.outTime ?: ""}\",\"$breakStr\",\"$workHours\",\"$statusLabel\",")
            sb.append("\"${if (d.lateMinutes > 0) d.lateMinutes else ""}\",\"${if (d.overtimeMinutes > 0) d.overtimeMinutes else ""}\",")
            sb.append("\"${if (d.mealAwarded > 0) 1 else ""}\",\"${d.note}\"\n")
        }

        sb.append("\n\"الإجمالي\",,,,\"\",\"${calc.totalLateMinutes}\",\"${calc.totalOvertimeMinutes}\",\"${calc.totalMealsCount}\",\"\"\n\n")
        sb.append("\"البيان\",\"القيمة\"\n")
        sb.append("\"المرتب الأساسي\",\"${calc.basicSalary}\"\n")
        sb.append("\"ساعات الإضافي\",\"${String.format(java.util.Locale.US, "%.2f", calc.overtimeHours)}\"\n")
        sb.append("\"قيمة الإضافي\",\"${calc.overtimeValue}\"\n")
        sb.append("\"بدل الوجبات\",\"${calc.mealsValue}\"\n")
        sb.append("\"حوافز الإنتاج\",\"${calc.productionBonus}\"\n")
        sb.append("\"إجمالي الإضافات\",\"${calc.totalAdditions}\"\n")
        sb.append("\"صافي المرتب\",\"${calc.grossSalary}\"\n")
        sb.append("\"خصم الغياب\",\"${calc.absenceDeduction}\"\n")
        sb.append("\"خصم التأخير\",\"${calc.lateDeduction}\"\n")
        sb.append("\"سداد سلفة\",\"${calc.loanDeduction}\"\n")
        sb.append("\"إجمالي الاستقطاعات\",\"${calc.totalDeductions}\"\n")
        sb.append("\"الصافي النهائي\",\"${calc.netSalary}\"\n")

        return sb.toString()
    }

    suspend fun login(email: String, password: String, rememberMe: Boolean): com.example.data.model.AuthResult {
        return withContext(Dispatchers.IO) {
            val cleanEmail = email.trim().lowercase()
            if (cleanEmail.isBlank()) {
                return@withContext com.example.data.model.AuthResult.Error("يرجى إدخال البريد الإلكتروني")
            }
            if (password.isBlank()) {
                return@withContext com.example.data.model.AuthResult.Error("يرجى إدخال كلمة المرور")
            }
            val user = dao.getUserByEmail(cleanEmail)
                ?: return@withContext com.example.data.model.AuthResult.Error("البريد الإلكتروني غير مسجل في النظام")

            val isValid = com.example.util.PasswordSecurity.verifyPassword(password, user.salt, user.passwordHash)
            if (!isValid) {
                return@withContext com.example.data.model.AuthResult.Error("كلمة المرور غير صحيحة، يرجى المحاولة مرة أخرى")
            }

            val empEntity = dao.getEmployeeById(user.employeeId)
                ?: return@withContext com.example.data.model.AuthResult.Error("تعذر العثور على ملف الموظف المرتبط بهذا الحساب")

            sessionManager.saveSession(user.email, user.id, rememberMe)
            com.example.data.model.AuthResult.Success(user.toDomain(), empEntity.toDomain())
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        employeeCode: String,
        password: String,
        confirmPassword: String,
        rememberMe: Boolean
    ): com.example.data.model.RegisterResult {
        return withContext(Dispatchers.IO) {
            val cleanName = fullName.trim()
            val cleanEmail = email.trim().lowercase()
            val cleanCode = employeeCode.trim()

            if (cleanName.isBlank()) {
                return@withContext com.example.data.model.RegisterResult.Error("يرجى إدخال الاسم الكامل")
            }
            val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
            if (!emailRegex.matches(cleanEmail)) {
                return@withContext com.example.data.model.RegisterResult.Error("صيغة البريد الإلكتروني غير صالحة")
            }
            if (cleanCode.isBlank()) {
                return@withContext com.example.data.model.RegisterResult.Error("يرجى إدخال كود الاعتماد الوظيفي الخاص بك")
            }
            if (password.length < 6) {
                return@withContext com.example.data.model.RegisterResult.Error("يجب أن تتكون كلمة المرور من 6 أحرف على الأقل")
            }
            if (password != confirmPassword) {
                return@withContext com.example.data.model.RegisterResult.Error("كلمتا المرور غير متطابقتين")
            }

            // Check if email is already registered
            val existing = dao.getUserByEmail(cleanEmail)
            if (existing != null) {
                return@withContext com.example.data.model.RegisterResult.Error("هذا البريد الإلكتروني مسجل بالفعل لموظف آخر")
            }

            // Check authorization: Must match an authorized employee or company passcodes (e.g. 1024, EMP-2026, HR-2026)
            val allEmployees = dao.getAllEmployeesSync()
            var matchedEmp = allEmployees.find { emp ->
                val codeStr = if (emp.id > 0) String.format("%04d", emp.id) else "1024"
                codeStr.equals(cleanCode, ignoreCase = true) || cleanCode.equals(emp.id.toString(), ignoreCase = true)
            }

            val isMasterAuthorizedCode = cleanCode.equals("EMP-2026", ignoreCase = true) ||
                    cleanCode.equals("HR-2026", ignoreCase = true) ||
                    cleanCode.equals("1024", ignoreCase = true)

            if (matchedEmp == null && !isMasterAuthorizedCode) {
                return@withContext com.example.data.model.RegisterResult.Error("كود الاعتماد الوظيفي غير مصرح به. التسجيل متاح فقط للموظفين المعتمدين من إدارة الشركة.")
            }

            if (matchedEmp != null) {
                val existingAcc = dao.getUserByEmployeeId(matchedEmp.id)
                if (existingAcc != null) {
                    if (isMasterAuthorizedCode) {
                        matchedEmp = null
                    } else {
                        return@withContext com.example.data.model.RegisterResult.Error("هذا الموظف لديه حساب مسجل مسبقاً، يمكنك تسجيل الدخول أو استعادة كلمة المرور")
                    }
                }
            }

            val targetEmployeeId = if (matchedEmp != null) {
                matchedEmp.id
            } else {
                val defaults = DateUtils.getDefaultDateRange()
                val newEmp = EmployeeEntity(
                    name = cleanName,
                    salary = 6500.0,
                    divisorDays = 30.0,
                    startTime = "09:00",
                    endTime = "17:00",
                    overtimeMultiplier = 1.5,
                    mealValue = 20.0,
                    mealThresholdTime = "22:00",
                    productionBonus = 300.0,
                    loanRepayment = 0.0,
                    personalPrint = 0.0,
                    delayDeductionPerMin = 1.0,
                    absenceMultiplier = 1.0,
                    startDate = defaults.first,
                    endDate = defaults.second,
                    holidaysCsv = "",
                    accentColorHex = "#8B5CF6",
                    jobTitle = "موظف معتمد",
                    department = "العمليات",
                    hireDate = DateUtils.todayIso(),
                    annualLeaveBalance = 14,
                    sickLeaveBalance = 12,
                    permissionHoursBalance = 4
                )
                dao.insertEmployee(newEmp)
            }

            val salt = com.example.util.PasswordSecurity.generateSalt()
            val hash = com.example.util.PasswordSecurity.hashPassword(password, salt)
            val userEntity = com.example.data.local.UserAccountEntity(
                employeeId = targetEmployeeId,
                email = cleanEmail,
                passwordHash = hash,
                salt = salt,
                fullName = cleanName,
                employeeCode = cleanCode,
                createdAt = DateUtils.todayIso()
            )
            val newUserId = dao.insertUser(userEntity)
            val emp = dao.getEmployeeById(targetEmployeeId)!!.toDomain()

            sessionManager.saveSession(cleanEmail, newUserId, rememberMe)
            com.example.data.model.RegisterResult.Success(userEntity.copy(id = newUserId).toDomain(), emp)
        }
    }

    suspend fun requestPasswordReset(email: String): com.example.data.model.ResetPasswordResult {
        return withContext(Dispatchers.IO) {
            val cleanEmail = email.trim().lowercase()
            val user = dao.getUserByEmail(cleanEmail)
                ?: return@withContext com.example.data.model.ResetPasswordResult.Error("هذا البريد الإلكتروني غير مسجل في النظام")

            val resetCode = com.example.util.PasswordSecurity.generateResetCode()
            dao.updateUser(user.copy(resetCode = resetCode))
            com.example.data.model.ResetPasswordResult.CodeSent(
                message = "تم إرسال رمز استعادة كلمة المرور بنجاح إلى $cleanEmail",
                simulatedCode = resetCode
            )
        }
    }

    suspend fun completePasswordReset(
        email: String,
        resetCode: String,
        newPassword: String,
        confirmPassword: String
    ): com.example.data.model.ResetPasswordResult {
        return withContext(Dispatchers.IO) {
            val cleanEmail = email.trim().lowercase()
            val user = dao.getUserByEmail(cleanEmail)
                ?: return@withContext com.example.data.model.ResetPasswordResult.Error("المستخدم غير مسجل")

            if (user.resetCode.isNullOrBlank() || user.resetCode != resetCode.trim()) {
                return@withContext com.example.data.model.ResetPasswordResult.Error("رمز التحقق غير صحيح أو منتهي الصلاحية")
            }
            if (newPassword.length < 6) {
                return@withContext com.example.data.model.ResetPasswordResult.Error("يجب أن تتكون كلمة المرور من 6 أحرف على الأقل")
            }
            if (newPassword != confirmPassword) {
                return@withContext com.example.data.model.ResetPasswordResult.Error("كلمتا المرور غير متطابقتين")
            }

            val newSalt = com.example.util.PasswordSecurity.generateSalt()
            val newHash = com.example.util.PasswordSecurity.hashPassword(newPassword, newSalt)
            dao.updateUser(
                user.copy(
                    passwordHash = newHash,
                    salt = newSalt,
                    resetCode = null
                )
            )
            com.example.data.model.ResetPasswordResult.Success("تم تغيير كلمة المرور بنجاح. يمكنك الآن تسجيل الدخول.")
        }
    }

    suspend fun checkAutoLogin(): Pair<com.example.data.model.UserAccount, Employee>? {
        return withContext(Dispatchers.IO) {
            if (!sessionManager.isRemembered()) return@withContext null
            val savedEmail = sessionManager.getSavedEmail() ?: return@withContext null
            val user = dao.getUserByEmail(savedEmail) ?: return@withContext null
            val emp = dao.getEmployeeById(user.employeeId)?.toDomain() ?: return@withContext null
            Pair(user.toDomain(), emp)
        }
    }

    // Notifications
    fun getNotifications(employeeId: Long): Flow<List<com.example.data.model.AppNotification>> {
        return dao.getNotificationsForEmployee(employeeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun sendNotification(notification: com.example.data.model.AppNotification): Long {
        return withContext(Dispatchers.IO) {
            dao.insertNotification(notification.toEntity())
        }
    }

    suspend fun markNotificationAsRead(id: Long) {
        withContext(Dispatchers.IO) {
            dao.markNotificationAsRead(id)
        }
    }

    suspend fun markAllNotificationsAsRead(employeeId: Long) {
        withContext(Dispatchers.IO) {
            dao.markAllNotificationsAsRead(employeeId)
        }
    }

    suspend fun clearNotifications(employeeId: Long) {
        withContext(Dispatchers.IO) {
            dao.clearNotificationsForEmployee(employeeId)
        }
    }

    // HR Tickets / Inquiries
    fun getHrTickets(employeeId: Long): Flow<List<com.example.data.model.HrTicket>> {
        return dao.getTicketsForEmployee(employeeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllHrTickets(): Flow<List<com.example.data.model.HrTicket>> {
        return dao.getAllTickets().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun submitHrTicket(ticket: com.example.data.model.HrTicket): Long {
        return withContext(Dispatchers.IO) {
            val id = dao.insertTicket(ticket.toEntity())
            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = ticket.employeeId,
                    title = "تم إرسال تذكرتك للإدارة",
                    message = "تم تسجيل طلبك: '${ticket.subject}' وسيقوم مسؤول الموارد البشرية بالرد قريباً.",
                    type = "system",
                    timestamp = System.currentTimeMillis()
                )
            )
            id
        }
    }

    suspend fun respondToHrTicket(ticketId: Long, response: String, newStatus: String = "answered") {
        withContext(Dispatchers.IO) {
            val tickets = dao.getAllTickets().first()
            val target = tickets.find { it.id == ticketId } ?: return@withContext
            val updated = target.copy(
                adminResponse = response,
                status = newStatus,
                updatedAt = DateUtils.todayIso()
            )
            dao.updateTicket(updated)

            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = target.employeeId,
                    title = "رد جديد من إدارة الموارد البشرية",
                    message = "تم الرد على استفسارك: '${target.subject}'",
                    type = "system",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteHrTicket(ticketId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteTicket(ticketId)
        }
    }

    // Offline Synchronization
    suspend fun getPendingSyncCount(employeeId: Long): Int {
        return withContext(Dispatchers.IO) {
            dao.getPendingSyncRecords(employeeId).size
        }
    }

    suspend fun syncPendingRecords(employeeId: Long): Int {
        return withContext(Dispatchers.IO) {
            val pending = dao.getPendingSyncRecords(employeeId)
            if (pending.isEmpty()) return@withContext 0
            val ids = pending.map { it.id }
            dao.markRecordsAsSynced(ids)

            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = employeeId,
                    title = "اكتملت مزامنة السجلات",
                    message = "تمت مزامنة ${pending.size} بصمة وسجل حضور بنجاح مع السيرفر السحابي.",
                    type = "system",
                    timestamp = System.currentTimeMillis()
                )
            )
            pending.size
        }
    }

    // Admin Access Queries
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>> {
        return dao.getAllLeaveRequests().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllCorrectionRequests(): Flow<List<CorrectionRequest>> {
        return dao.getAllCorrectionRequests().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun updateLeaveRequestStatus(requestId: Long, status: String) {
        withContext(Dispatchers.IO) {
            val leaves = dao.getAllLeaveRequests().first()
            val target = leaves.find { it.id == requestId } ?: return@withContext
            dao.updateLeaveRequest(target.copy(status = status))

            val statusArabic = if (status == "approved") "تمت الموافقة على" else "تم رفض"
            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = target.employeeId,
                    title = "$statusArabic طلب الإجازة",
                    message = "$statusArabic طلبك (${target.typeArabic}) للفترة من ${target.startDate} إلى ${target.endDate}",
                    type = "leave",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateCorrectionRequestStatus(requestId: Long, status: String) {
        withContext(Dispatchers.IO) {
            val corrections = dao.getAllCorrectionRequests().first()
            val target = corrections.find { it.id == requestId } ?: return@withContext
            dao.updateCorrectionRequest(target.copy(status = status))

            if (status == "approved") {
                val records = dao.getDayRecordsForEmployeeSync(target.employeeId).associateBy { it.dateKey }
                val rec = records[target.dateKey] ?: DayRecordEntity(employeeId = target.employeeId, dateKey = target.dateKey)
                val updatedRec = rec.copy(
                    inTime = target.requestedIn ?: rec.inTime,
                    outTime = target.requestedOut ?: rec.outTime
                )
                dao.insertOrUpdateDayRecord(updatedRec)
            }

            val statusArabic = if (status == "approved") "تم قبول" else "تم رفض"
            dao.insertNotification(
                com.example.data.local.AppNotificationEntity(
                    employeeId = target.employeeId,
                    title = "$statusArabic طلب تصحيح البصمة",
                    message = "$statusArabic طلب تصحيح البصمة ليوم ${target.dateKey}",
                    type = "correction",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}

// Converters
private fun EmployeeEntity.toDomain(): Employee {
    val hols = if (holidaysCsv.isNotBlank()) holidaysCsv.split(",").map { it.trim() } else emptyList()
    return Employee(
        id = id,
        name = name,
        settings = EmployeeSettings(
            salary = salary,
            divisorDays = divisorDays,
            start = startTime,
            end = endTime,
            mult = overtimeMultiplier,
            mealVal = mealValue,
            mealTime = mealThresholdTime,
            inc = productionBonus,
            loan = loanRepayment,
            prt = personalPrint,
            delay = delayDeductionPerMin,
            absM = absenceMultiplier,
            d1 = startDate,
            d2 = endDate,
            holidays = hols,
            accentColor = accentColorHex,
            jobTitle = jobTitle,
            department = department,
            hireDate = hireDate,
            annualLeaveBalance = annualLeaveBalance,
            sickLeaveBalance = sickLeaveBalance,
            permissionHoursBalance = permissionHoursBalance,
            workLat = workLat,
            workLng = workLng,
            geofenceRadiusMeters = geofenceRadiusMeters,
            geofenceEnabled = geofenceEnabled,
            userRole = userRole
        )
    )
}

private fun Employee.toEntity(): EmployeeEntity {
    return EmployeeEntity(
        id = id,
        name = name,
        salary = settings.salary,
        divisorDays = settings.divisorDays,
        startTime = settings.start,
        endTime = settings.end,
        overtimeMultiplier = settings.mult,
        mealValue = settings.mealVal,
        mealThresholdTime = settings.mealTime,
        productionBonus = settings.inc,
        loanRepayment = settings.loan,
        personalPrint = settings.prt,
        delayDeductionPerMin = settings.delay,
        absenceMultiplier = settings.absM,
        startDate = settings.d1,
        endDate = settings.d2,
        holidaysCsv = settings.holidays.joinToString(","),
        accentColorHex = settings.accentColor,
        jobTitle = settings.jobTitle,
        department = settings.department,
        hireDate = settings.hireDate,
        annualLeaveBalance = settings.annualLeaveBalance,
        sickLeaveBalance = settings.sickLeaveBalance,
        permissionHoursBalance = settings.permissionHoursBalance,
        workLat = settings.workLat,
        workLng = settings.workLng,
        geofenceRadiusMeters = settings.geofenceRadiusMeters,
        geofenceEnabled = settings.geofenceEnabled,
        userRole = settings.userRole
    )
}

private fun DayRecordEntity.toDomain(): DayRecord {
    return DayRecord(
        id = id,
        employeeId = employeeId,
        dateKey = dateKey,
        inTime = inTime,
        outTime = outTime,
        breakStart = breakStart,
        breakEnd = breakEnd,
        status = status,
        meal = mealSetting,
        note = note,
        punchLat = punchLat,
        punchLng = punchLng,
        withinGeofence = withinGeofence,
        syncStatus = syncStatus
    )
}

private fun DayRecord.toEntity(): DayRecordEntity {
    return DayRecordEntity(
        id = id,
        employeeId = employeeId,
        dateKey = dateKey,
        inTime = inTime,
        outTime = outTime,
        breakStart = breakStart,
        breakEnd = breakEnd,
        status = status,
        mealSetting = meal,
        note = note,
        punchLat = punchLat,
        punchLng = punchLng,
        withinGeofence = withinGeofence,
        syncStatus = syncStatus
    )
}

private fun LeaveRequestEntity.toDomain(): LeaveRequest {
    return LeaveRequest(
        id = id,
        employeeId = employeeId,
        typeArabic = typeArabic,
        startDate = startDate,
        endDate = endDate,
        durationText = durationText,
        reason = reason,
        status = status,
        createdAt = createdAt
    )
}

private fun LeaveRequest.toEntity(): LeaveRequestEntity {
    return LeaveRequestEntity(
        id = id,
        employeeId = employeeId,
        typeArabic = typeArabic,
        startDate = startDate,
        endDate = endDate,
        durationText = durationText,
        reason = reason,
        status = status,
        createdAt = createdAt
    )
}

private fun CorrectionRequestEntity.toDomain(): CorrectionRequest {
    return CorrectionRequest(
        id = id,
        employeeId = employeeId,
        dateKey = dateKey,
        requestedIn = requestedIn,
        requestedOut = requestedOut,
        reason = reason,
        status = status,
        createdAt = createdAt
    )
}

private fun CorrectionRequest.toEntity(): CorrectionRequestEntity {
    return CorrectionRequestEntity(
        id = id,
        employeeId = employeeId,
        dateKey = dateKey,
        requestedIn = requestedIn,
        requestedOut = requestedOut,
        reason = reason,
        status = status,
        createdAt = createdAt
    )
}

private fun com.example.data.local.UserAccountEntity.toDomain(): com.example.data.model.UserAccount {
    return com.example.data.model.UserAccount(
        id = id,
        employeeId = employeeId,
        email = email,
        fullName = fullName,
        employeeCode = employeeCode,
        createdAt = createdAt
    )
}

private fun com.example.data.local.AppNotificationEntity.toDomain(): com.example.data.model.AppNotification {
    return com.example.data.model.AppNotification(
        id = id,
        employeeId = employeeId,
        title = title,
        message = message,
        type = type,
        timestamp = timestamp,
        isRead = isRead
    )
}

private fun com.example.data.model.AppNotification.toEntity(): com.example.data.local.AppNotificationEntity {
    return com.example.data.local.AppNotificationEntity(
        id = id,
        employeeId = employeeId,
        title = title,
        message = message,
        type = type,
        timestamp = timestamp,
        isRead = isRead
    )
}

private fun com.example.data.local.HrTicketEntity.toDomain(): com.example.data.model.HrTicket {
    return com.example.data.model.HrTicket(
        id = id,
        employeeId = employeeId,
        employeeName = employeeName,
        category = category,
        subject = subject,
        message = message,
        priority = priority,
        status = status,
        adminResponse = adminResponse,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun com.example.data.model.HrTicket.toEntity(): com.example.data.local.HrTicketEntity {
    return com.example.data.local.HrTicketEntity(
        id = id,
        employeeId = employeeId,
        employeeName = employeeName,
        category = category,
        subject = subject,
        message = message,
        priority = priority,
        status = status,
        adminResponse = adminResponse,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

