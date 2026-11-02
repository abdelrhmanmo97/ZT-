package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNavTab
import com.example.data.model.AppNotification
import com.example.data.model.AttendanceFilter
import com.example.data.model.CorrectionRequest
import com.example.data.model.DayCalculation
import com.example.data.model.DayRecord
import com.example.data.model.Employee
import com.example.data.model.EmployeeSettings
import com.example.data.model.HrTicket
import com.example.data.model.LeaveRequest
import com.example.data.model.LocationStatus
import com.example.data.model.SalaryCalculation
import com.example.data.repository.SalaryRepository
import com.example.util.DateUtils
import com.example.util.LocationHelper
import com.example.util.NetworkMonitor
import com.example.util.NotificationHelper
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SalaryUiState(
    val selectedTab: AppNavTab = AppNavTab.HOME,
    val employees: List<Employee> = emptyList(),
    val currentEmployee: Employee? = null,
    val currentUser: com.example.data.model.UserAccount? = null,
    val dayRecords: Map<String, DayRecord> = emptyMap(),
    val dayCalculations: List<DayCalculation> = emptyList(),
    val salarySummary: SalaryCalculation = SalaryCalculation(),
    val leaveRequests: List<LeaveRequest> = emptyList(),
    val correctionRequests: List<CorrectionRequest> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val hrTickets: List<HrTicket> = emptyList(),
    val allHrTickets: List<HrTicket> = emptyList(),
    val allLeaveRequests: List<LeaveRequest> = emptyList(),
    val allCorrectionRequests: List<CorrectionRequest> = emptyList(),
    val locationStatus: LocationStatus = LocationStatus(),
    val isOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
    val filter: AttendanceFilter = AttendanceFilter.ALL,
    val message: String? = null,
    val isDarkTheme: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authError: String? = null,
    val authSuccess: String? = null,
    val simulatedResetCode: String? = null,
    val resetCodeSent: Boolean = false,
    val showDaysList: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showEmployeeDialog: Boolean = false,
    val showShareExportDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showBiometricPunchDialog: Boolean = false,
    val showLoginDialog: Boolean = false,
    val showLeaveRequestDialog: Boolean = false,
    val showCorrectionDialog: Boolean = false,
    val showHrSupportDialog: Boolean = false,
    val showNotificationCenterDialog: Boolean = false,
    val showHrTicketDialog: Boolean = false,
    val showAdminDashboardDialog: Boolean = false,
    val showGeofenceDetailsDialog: Boolean = false,
    val isAdminMode: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val timePickerTarget: Pair<String, String>? = null // dateKey to field ("in" or "out")
) {
    val activeEmployee: Employee
        get() = currentEmployee ?: Employee(name = currentUser?.fullName ?: "محمد أحمد")

    val todayCalculation: DayCalculation?
        get() {
            val todayKey = DateUtils.todayKey()
            return dayCalculations.find { it.dateKey == todayKey }
        }

    val pendingRequestsCount: Int
        get() = leaveRequests.count { it.status == "pending" } + correctionRequests.count { it.status == "pending" }
}

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SalaryRepository.getInstance(application)
    private val networkMonitor = NetworkMonitor(application)

    private val _uiState = MutableStateFlow(SalaryUiState())
    val uiState: StateFlow<SalaryUiState> = _uiState.asStateFlow()

    private val _currentEmployeeId = MutableStateFlow<Long?>(null)
    private val _currentFilter = MutableStateFlow(AttendanceFilter.ALL)
    private val _isDark = MutableStateFlow(true)
    private val _message = MutableStateFlow<String?>(null)

    init {
        NotificationHelper.createNotificationChannel(application)

        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                val prev = _uiState.value.isOnline
                _uiState.value = _uiState.value.copy(isOnline = online)
                if (!prev && online) {
                    val emp = _uiState.value.currentEmployee
                    if (emp != null) {
                        syncPendingOfflineRecords(silent = true)
                    }
                }
            }
        }

        viewModelScope.launch {
            val first = repository.getInitialOrFirstEmployee()
            val autoLogin = repository.checkAutoLogin()
            if (autoLogin != null) {
                _currentEmployeeId.value = autoLogin.second.id
                _uiState.value = _uiState.value.copy(
                    currentUser = autoLogin.first,
                    currentEmployee = autoLogin.second,
                    isLoggedIn = true,
                    isAdminMode = autoLogin.second.isAdmin
                )
            } else {
                _currentEmployeeId.value = first.id
                _uiState.value = _uiState.value.copy(
                    currentEmployee = first,
                    isLoggedIn = false
                )
            }
            refreshLocation()
        }

        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getAllEmployees().collect { empList ->
                if (empList.isEmpty()) {
                    val initial = repository.getInitialOrFirstEmployee()
                    _currentEmployeeId.value = initial.id
                    return@collect
                }
                val curId = _currentEmployeeId.value ?: empList.first().id
                val active = empList.find { it.id == curId } ?: empList.first()
                if (_currentEmployeeId.value != active.id) {
                    _currentEmployeeId.value = active.id
                }

                launch {
                    repository.getDayRecords(active.id).collect { records ->
                        val (dayCalcs, summary) = repository.calculateDays(active, records)
                        val pendingSync = repository.getPendingSyncCount(active.id)
                        _uiState.value = _uiState.value.copy(
                            employees = empList,
                            currentEmployee = active,
                            dayRecords = records,
                            dayCalculations = dayCalcs,
                            salarySummary = summary,
                            pendingSyncCount = pendingSync,
                            filter = _currentFilter.value,
                            isDarkTheme = _isDark.value,
                            message = _message.value
                        )
                    }
                }

                launch {
                    repository.getLeaveRequests(active.id).collect { leaves ->
                        _uiState.value = _uiState.value.copy(leaveRequests = leaves)
                    }
                }

                launch {
                    repository.getCorrectionRequests(active.id).collect { corrections ->
                        _uiState.value = _uiState.value.copy(correctionRequests = corrections)
                    }
                }

                launch {
                    repository.getNotifications(active.id).collect { notifs ->
                        val unread = notifs.count { !it.isRead }
                        _uiState.value = _uiState.value.copy(
                            notifications = notifs,
                            unreadNotificationCount = unread
                        )
                    }
                }

                launch {
                    repository.getHrTickets(active.id).collect { tickets ->
                        _uiState.value = _uiState.value.copy(hrTickets = tickets)
                    }
                }

                launch {
                    repository.getAllHrTickets().collect { allTickets ->
                        _uiState.value = _uiState.value.copy(allHrTickets = allTickets)
                    }
                }

                launch {
                    repository.getAllLeaveRequests().collect { allLeaves ->
                        _uiState.value = _uiState.value.copy(allLeaveRequests = allLeaves)
                    }
                }

                launch {
                    repository.getAllCorrectionRequests().collect { allCorrections ->
                        _uiState.value = _uiState.value.copy(allCorrectionRequests = allCorrections)
                    }
                }
            }
        }
    }

    fun selectTab(tab: AppNavTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleDaysList() {
        _uiState.value = _uiState.value.copy(showDaysList = !_uiState.value.showDaysList)
    }

    fun selectEmployee(employee: Employee) {
        _currentEmployeeId.value = employee.id
        viewModelScope.launch {
            launch {
                repository.getDayRecords(employee.id).collect { records ->
                    val (dayCalcs, summary) = repository.calculateDays(employee, records)
                    val pendingSync = repository.getPendingSyncCount(employee.id)
                    _uiState.value = _uiState.value.copy(
                        currentEmployee = employee,
                        dayRecords = records,
                        dayCalculations = dayCalcs,
                        salarySummary = summary,
                        pendingSyncCount = pendingSync
                    )
                }
            }
            launch {
                repository.getLeaveRequests(employee.id).collect { leaves ->
                    _uiState.value = _uiState.value.copy(leaveRequests = leaves)
                }
            }
            launch {
                repository.getCorrectionRequests(employee.id).collect { corrections ->
                    _uiState.value = _uiState.value.copy(correctionRequests = corrections)
                }
            }
            launch {
                repository.getNotifications(employee.id).collect { notifs ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifs,
                        unreadNotificationCount = notifs.count { !it.isRead }
                    )
                }
            }
            launch {
                repository.getHrTickets(employee.id).collect { tickets ->
                    _uiState.value = _uiState.value.copy(hrTickets = tickets)
                }
            }
        }
    }

    fun addEmployee(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val defaults = DateUtils.getDefaultDateRange()
            val newEmp = Employee(
                name = name.trim(),
                settings = EmployeeSettings(
                    d1 = defaults.first,
                    d2 = defaults.second
                )
            )
            val id = repository.saveEmployee(newEmp)
            _currentEmployeeId.value = id
            showMessage("تمت إضافة الموظف: $name")
        }
    }

    fun renameEmployee(employee: Employee, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.saveEmployee(employee.copy(name = newName.trim()))
            showMessage("تم تعديل الاسم")
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            val list = _uiState.value.employees
            if (list.size <= 1) {
                showMessage("لا يمكن حذف الموظف الوحيد")
                return@launch
            }
            repository.deleteEmployee(employee)
            val remaining = list.filter { it.id != employee.id }
            selectEmployee(remaining.first())
            showMessage("تم حذف الموظف")
        }
    }

    fun refreshLocation(simulateOutside: Boolean = false) {
        val emp = _uiState.value.currentEmployee
        val workLat = emp?.settings?.workLat ?: LocationHelper.DEFAULT_WORK_LAT
        val workLng = emp?.settings?.workLng ?: LocationHelper.DEFAULT_WORK_LNG
        val radius = emp?.settings?.geofenceRadiusMeters ?: LocationHelper.DEFAULT_GEOFENCE_RADIUS_METERS

        val (curLat, curLng) = if (simulateOutside) {
            Pair(workLat + 0.035, workLng + 0.035) // ~3.5km away
        } else {
            Pair(workLat + 0.00015, workLng + 0.00015) // ~20m away (inside workplace)
        }
        val dist = LocationHelper.calculateDistanceMeters(curLat, curLng, workLat, workLng)
        val within = dist <= radius

        _uiState.value = _uiState.value.copy(
            locationStatus = LocationStatus(
                currentLat = curLat,
                currentLng = curLng,
                distanceMeters = dist,
                isWithinGeofence = within,
                isSimulated = simulateOutside,
                lastCheckedTime = DateUtils.currentTimeHm()
            )
        )
    }

    fun punchIn() {
        val emp = _uiState.value.currentEmployee ?: return
        val loc = _uiState.value.locationStatus
        val isOnline = _uiState.value.isOnline
        viewModelScope.launch {
            val res = repository.punchIn(
                employee = emp,
                punchLat = loc.currentLat,
                punchLng = loc.currentLng,
                withinGeofence = loc.isWithinGeofence,
                isOnline = isOnline
            )
            NotificationHelper.showSystemNotification(
                context = getApplication(),
                notificationId = 101,
                title = "تسجيل الحضور (بصمة العمل)",
                message = res
            )
            val pending = repository.getPendingSyncCount(emp.id)
            _uiState.value = _uiState.value.copy(pendingSyncCount = pending)
            showMessage(res)
        }
    }

    fun punchOut() {
        val emp = _uiState.value.currentEmployee ?: return
        val loc = _uiState.value.locationStatus
        val isOnline = _uiState.value.isOnline
        viewModelScope.launch {
            val res = repository.punchOut(
                employee = emp,
                punchLat = loc.currentLat,
                punchLng = loc.currentLng,
                withinGeofence = loc.isWithinGeofence,
                isOnline = isOnline
            )
            NotificationHelper.showSystemNotification(
                context = getApplication(),
                notificationId = 102,
                title = "تسجيل الانصراف (بصمة العمل)",
                message = res
            )
            val pending = repository.getPendingSyncCount(emp.id)
            _uiState.value = _uiState.value.copy(pendingSyncCount = pending)
            showMessage(res)
        }
    }

    fun syncPendingOfflineRecords(silent: Boolean = false) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val count = repository.syncPendingRecords(emp.id)
            _uiState.value = _uiState.value.copy(pendingSyncCount = 0)
            if (!silent) {
                if (count > 0) {
                    showMessage("تمت مزامنة $count بصمة مع السيرفر بنجاح ✓")
                } else {
                    showMessage("جميع السجلات متزامنة بالفعل ✓")
                }
            }
        }
    }

    // Notification methods
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(emp.id)
            showMessage("تم تحديد جميع الإشعارات كمقروءة")
        }
    }

    fun clearAllNotifications() {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            repository.clearNotifications(emp.id)
            showMessage("تم مسح سجل الإشعارات")
        }
    }

    fun toggleNotificationCenterDialog() {
        _uiState.value = _uiState.value.copy(
            showNotificationCenterDialog = !_uiState.value.showNotificationCenterDialog
        )
    }

    // HR Tickets methods
    fun submitHrTicket(
        category: String,
        subject: String,
        message: String,
        priority: String
    ) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val ticket = HrTicket(
                employeeId = emp.id,
                employeeName = emp.name,
                category = category,
                subject = subject,
                message = message,
                priority = priority,
                status = "pending",
                createdAt = DateUtils.todayIso()
            )
            repository.submitHrTicket(ticket)
            showMessage("تم إرسال تذكرتك بنجاح للإدارة")
            _uiState.value = _uiState.value.copy(showHrTicketDialog = false)
        }
    }

    fun respondToHrTicket(ticketId: Long, response: String, status: String = "answered") {
        viewModelScope.launch {
            repository.respondToHrTicket(ticketId, response, status)
            showMessage("تم إرسال الرد للموظف بنجاح")
        }
    }

    fun deleteHrTicket(ticketId: Long) {
        viewModelScope.launch {
            repository.deleteHrTicket(ticketId)
            showMessage("تم حذف التذكرة")
        }
    }

    fun toggleHrTicketDialog() {
        _uiState.value = _uiState.value.copy(
            showHrTicketDialog = !_uiState.value.showHrTicketDialog
        )
    }

    // Admin Dashboard & Approvals
    fun toggleAdminDashboardDialog() {
        _uiState.value = _uiState.value.copy(
            showAdminDashboardDialog = !_uiState.value.showAdminDashboardDialog
        )
    }

    fun setAdminMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAdminMode = enabled)
    }

    fun approveLeaveRequest(id: Long) {
        viewModelScope.launch {
            repository.updateLeaveRequestStatus(id, "approved")
            showMessage("تم قبول طلب الإجازة واعتماده")
        }
    }

    fun rejectLeaveRequest(id: Long) {
        viewModelScope.launch {
            repository.updateLeaveRequestStatus(id, "rejected")
            showMessage("تم رفض طلب الإجازة")
        }
    }

    fun approveCorrectionRequest(id: Long) {
        viewModelScope.launch {
            repository.updateCorrectionRequestStatus(id, "approved")
            showMessage("تم قبول تصحيح البصمة وتحديث السجل")
        }
    }

    fun rejectCorrectionRequest(id: Long) {
        viewModelScope.launch {
            repository.updateCorrectionRequestStatus(id, "rejected")
            showMessage("تم رفض طلب التصحيح")
        }
    }

    fun updateCompanyWorkplaceGps(lat: Double, lng: Double, radius: Double) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val updatedSettings = emp.settings.copy(
                workLat = lat,
                workLng = lng,
                geofenceRadiusMeters = radius
            )
            repository.saveEmployee(emp.copy(settings = updatedSettings))
            refreshLocation()
            showMessage("تم تحديث إحداثيات ونطاق مقر العمل بنجاح")
        }
    }

    // PDF Reports
    fun exportSalarySlipPdf() {
        val emp = _uiState.value.currentEmployee ?: return
        val calc = _uiState.value.salarySummary
        _uiState.value = _uiState.value.copy(isGeneratingPdf = true)
        viewModelScope.launch {
            try {
                val period = "${emp.settings.d1} إلى ${emp.settings.d2}"
                val file = PdfReportGenerator.generateSalaryPayslipPdf(
                    context = getApplication(),
                    employee = emp,
                    calc = calc,
                    monthPeriod = period
                )
                _uiState.value = _uiState.value.copy(isGeneratingPdf = false)
                PdfReportGenerator.openOrSharePdf(
                    context = getApplication(),
                    file = file,
                    title = "كشف مفردات المرتب - ${emp.name}"
                )
                NotificationHelper.showSystemNotification(
                    context = getApplication(),
                    notificationId = 103,
                    title = "تم إصدار كشف المرتب PDF",
                    message = "تم إنشاء ملف PDF بنجاح ويمكنك عرضه أو مشاركته الآن."
                )
                showMessage("تم إنشاء كشف المرتب PDF بنجاح")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isGeneratingPdf = false)
                showMessage("فشل إنشاء ملف PDF: ${e.localizedMessage}")
            }
        }
    }

    fun exportAttendanceLogPdf() {
        val emp = _uiState.value.currentEmployee ?: return
        val days = _uiState.value.dayCalculations
        _uiState.value = _uiState.value.copy(isGeneratingPdf = true)
        viewModelScope.launch {
            try {
                val period = "${emp.settings.d1} إلى ${emp.settings.d2}"
                val file = PdfReportGenerator.generateAttendanceLogPdf(
                    context = getApplication(),
                    employee = emp,
                    days = days,
                    monthPeriod = period
                )
                _uiState.value = _uiState.value.copy(isGeneratingPdf = false)
                PdfReportGenerator.openOrSharePdf(
                    context = getApplication(),
                    file = file,
                    title = "سجل الحضور والانصراف - ${emp.name}"
                )
                NotificationHelper.showSystemNotification(
                    context = getApplication(),
                    notificationId = 104,
                    title = "تم إصدار سجل الحضور PDF",
                    message = "تم إنشاء سجل الحضور الشهري بصيغة PDF بنجاح."
                )
                showMessage("تم إنشاء سجل الحضور PDF بنجاح")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isGeneratingPdf = false)
                showMessage("فشل إنشاء ملف PDF: ${e.localizedMessage}")
            }
        }
    }

    fun toggleBreak() {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val res = repository.toggleBreak(emp)
            showMessage(res)
        }
    }

    fun submitLeaveRequest(
        typeArabic: String,
        startDate: String,
        endDate: String,
        durationText: String,
        reason: String
    ) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val req = LeaveRequest(
                employeeId = emp.id,
                typeArabic = typeArabic,
                startDate = startDate,
                endDate = endDate,
                durationText = durationText,
                reason = reason,
                status = "pending",
                createdAt = DateUtils.todayIso()
            )
            repository.submitLeaveRequest(req)
            showMessage("تم تقديم الطلب بنجاح وهو قيد المراجعة")
        }
    }

    fun submitCorrectionRequest(
        dateKey: String,
        requestedIn: String?,
        requestedOut: String?,
        reason: String
    ) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val req = CorrectionRequest(
                employeeId = emp.id,
                dateKey = dateKey,
                requestedIn = requestedIn,
                requestedOut = requestedOut,
                reason = reason,
                status = "pending",
                createdAt = DateUtils.todayIso()
            )
            repository.submitCorrectionRequest(req)
            showMessage("تم إرسال طلب تصحيح البصمة للمراجعة")
        }
    }

    fun fillPastDays() {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val count = repository.fillPastDays(emp)
            showMessage("تم ملء $count يوم بمواعيد الدوام (بدون الجمعة)")
        }
    }

    fun quickFillDay(dateKey: String) {
        val emp = _uiState.value.currentEmployee ?: return
        val currentRec = _uiState.value.dayRecords[dateKey]
        val hasAny = currentRec != null && (!currentRec.inTime.isNullOrBlank() || !currentRec.outTime.isNullOrBlank() || currentRec.status.isNotBlank())
        if (hasAny) return

        viewModelScope.launch {
            repository.saveDayRecord(
                employeeId = emp.id,
                dateKey = dateKey,
                inTime = emp.settings.start,
                outTime = emp.settings.end,
                status = "",
                meal = "",
                note = currentRec?.note ?: ""
            )
        }
    }

    fun updateDayTime(dateKey: String, isCheckIn: Boolean, time: String?) {
        val emp = _uiState.value.currentEmployee ?: return
        val rec = _uiState.value.dayRecords[dateKey]
        val inTime = if (isCheckIn) time else rec?.inTime
        val outTime = if (!isCheckIn) time else rec?.outTime

        viewModelScope.launch {
            repository.saveDayRecord(
                employeeId = emp.id,
                dateKey = dateKey,
                inTime = inTime,
                outTime = outTime,
                status = rec?.status ?: "",
                meal = rec?.meal ?: "",
                note = rec?.note ?: ""
            )
        }
    }

    fun updateDayStatus(dateKey: String, status: String) {
        val emp = _uiState.value.currentEmployee ?: return
        val rec = _uiState.value.dayRecords[dateKey]
        viewModelScope.launch {
            repository.saveDayRecord(
                employeeId = emp.id,
                dateKey = dateKey,
                inTime = rec?.inTime,
                outTime = rec?.outTime,
                status = status,
                meal = rec?.meal ?: "",
                note = rec?.note ?: ""
            )
        }
    }

    fun updateDayMeal(dateKey: String, meal: String) {
        val emp = _uiState.value.currentEmployee ?: return
        val rec = _uiState.value.dayRecords[dateKey]
        viewModelScope.launch {
            repository.saveDayRecord(
                employeeId = emp.id,
                dateKey = dateKey,
                inTime = rec?.inTime,
                outTime = rec?.outTime,
                status = rec?.status ?: "",
                meal = meal,
                note = rec?.note ?: ""
            )
        }
    }

    fun updateDayNote(dateKey: String, note: String) {
        val emp = _uiState.value.currentEmployee ?: return
        val rec = _uiState.value.dayRecords[dateKey]
        viewModelScope.launch {
            repository.saveDayRecord(
                employeeId = emp.id,
                dateKey = dateKey,
                inTime = rec?.inTime,
                outTime = rec?.outTime,
                status = rec?.status ?: "",
                meal = rec?.meal ?: "",
                note = note
            )
        }
    }

    fun shiftMonth(months: Int) {
        val emp = _uiState.value.currentEmployee ?: return
        val s = emp.settings
        val newD1 = DateUtils.shiftMonth(s.d1, months)
        val newD2 = DateUtils.shiftMonth(s.d2, months)
        updateSettings(s.copy(d1 = newD1, d2 = newD2))
    }

    fun updateSettings(newSettings: EmployeeSettings) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val updated = emp.copy(settings = newSettings)
            repository.saveEmployee(updated)
            _uiState.value = _uiState.value.copy(currentEmployee = updated)
            recalculateForEmployee(updated)
        }
    }

    fun addHoliday(isoDate: String) {
        val emp = _uiState.value.currentEmployee ?: return
        if (isoDate.isBlank()) return
        if (!emp.settings.holidays.contains(isoDate)) {
            val updatedList = emp.settings.holidays + isoDate
            updateSettings(emp.settings.copy(holidays = updatedList))
            showMessage("تمت إضافة الإجازة الرسمية")
        }
    }

    fun removeHoliday(isoDate: String) {
        val emp = _uiState.value.currentEmployee ?: return
        val updatedList = emp.settings.holidays.filter { it != isoDate }
        updateSettings(emp.settings.copy(holidays = updatedList))
        showMessage("تم حذف الإجازة")
    }

    fun setFilter(filter: AttendanceFilter) {
        _currentFilter.value = filter
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun setAccentColor(colorHex: String) {
        val emp = _uiState.value.currentEmployee ?: return
        updateSettings(emp.settings.copy(accentColor = colorHex))
    }

    fun toggleDarkTheme() {
        _isDark.value = !_isDark.value
        _uiState.value = _uiState.value.copy(isDarkTheme = _isDark.value)
    }

    fun showMessage(msg: String) {
        _message.value = msg
        _uiState.value = _uiState.value.copy(message = msg)
    }

    fun clearMessage() {
        _message.value = null
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun setSettingsDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showSettingsDialog = visible)
    }

    fun setEmployeeDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showEmployeeDialog = visible)
    }

    fun setShareExportDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showShareExportDialog = visible)
    }

    fun setThemeDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showThemeDialog = visible)
    }

    fun setBiometricPunchDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showBiometricPunchDialog = visible)
    }

    fun setLoginDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showLoginDialog = visible)
    }

    fun setLeaveRequestDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showLeaveRequestDialog = visible)
    }

    fun setLeaveDialog(visible: Boolean) {
        setLeaveRequestDialog(visible)
    }

    fun setCorrectionDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showCorrectionDialog = visible)
    }

    fun setHrSupportDialog(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showHrSupportDialog = visible)
    }

    fun loginWithEmail(
        email: String,
        password: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isAuthLoading = true, authError = null, authSuccess = null)
        viewModelScope.launch {
            when (val res = repository.login(email, password, rememberMe)) {
                is com.example.data.model.AuthResult.Success -> {
                    _currentEmployeeId.value = res.employee.id
                    _uiState.value = _uiState.value.copy(
                        currentUser = res.user,
                        currentEmployee = res.employee,
                        isLoggedIn = true,
                        isAuthLoading = false,
                        authError = null,
                        authSuccess = "مرحباً بعودتك، ${res.employee.name}"
                    )
                    selectEmployee(res.employee)
                    onSuccess()
                }
                is com.example.data.model.AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authError = res.message
                    )
                }
            }
        }
    }

    fun registerWithEmail(
        fullName: String,
        email: String,
        employeeCode: String,
        password: String,
        confirmPassword: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isAuthLoading = true, authError = null, authSuccess = null)
        viewModelScope.launch {
            when (val res = repository.register(fullName, email, employeeCode, password, confirmPassword, rememberMe)) {
                is com.example.data.model.RegisterResult.Success -> {
                    _currentEmployeeId.value = res.employee.id
                    _uiState.value = _uiState.value.copy(
                        currentUser = res.user,
                        currentEmployee = res.employee,
                        isLoggedIn = true,
                        isAuthLoading = false,
                        authError = null,
                        authSuccess = "تم إنشاء الحساب بنجاح، مرحباً بك يا ${res.employee.name}"
                    )
                    selectEmployee(res.employee)
                    onSuccess()
                }
                is com.example.data.model.RegisterResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authError = res.message
                    )
                }
            }
        }
    }

    fun requestPasswordReset(email: String) {
        _uiState.value = _uiState.value.copy(isAuthLoading = true, authError = null, authSuccess = null)
        viewModelScope.launch {
            when (val res = repository.requestPasswordReset(email)) {
                is com.example.data.model.ResetPasswordResult.CodeSent -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        resetCodeSent = true,
                        simulatedResetCode = res.simulatedCode,
                        authSuccess = res.message,
                        authError = null
                    )
                }
                is com.example.data.model.ResetPasswordResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authError = res.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isAuthLoading = false)
                }
            }
        }
    }

    fun completePasswordReset(
        email: String,
        resetCode: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isAuthLoading = true, authError = null, authSuccess = null)
        viewModelScope.launch {
            when (val res = repository.completePasswordReset(email, resetCode, newPassword, confirmPassword)) {
                is com.example.data.model.ResetPasswordResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        resetCodeSent = false,
                        simulatedResetCode = null,
                        authSuccess = res.message,
                        authError = null
                    )
                    onSuccess()
                }
                is com.example.data.model.ResetPasswordResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authError = res.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isAuthLoading = false)
                }
            }
        }
    }

    fun clearAuthMessages() {
        _uiState.value = _uiState.value.copy(authError = null, authSuccess = null)
    }

    fun loginEmployee(employee: Employee) {
        selectEmployee(employee)
        _uiState.value = _uiState.value.copy(isLoggedIn = true)
        showMessage("تم تسجيل الدخول بنجاح كـ ${employee.name}")
    }

    fun logout() {
        repository.logout()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            currentUser = null,
            authError = null,
            authSuccess = null
        )
        showMessage("تم تسجيل الخروج بنجاح")
    }

    fun setTimePickerTarget(target: Pair<String, String>?) {
        _uiState.value = _uiState.value.copy(timePickerTarget = target)
    }

    fun exportJson(): String {
        val emp = _uiState.value.currentEmployee ?: return "{}"
        return repository.exportToJson(emp, _uiState.value.dayRecords)
    }

    fun exportCsv(): String {
        val emp = _uiState.value.currentEmployee ?: return ""
        return repository.buildCsv(emp, _uiState.value.dayCalculations, _uiState.value.salarySummary)
    }

    fun exportTextSummary(): String {
        val emp = _uiState.value.currentEmployee ?: return ""
        return repository.buildSummaryText(emp, _uiState.value.dayCalculations, _uiState.value.salarySummary)
    }

    fun importJson(json: String) {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            val res = repository.importFromJson(json, emp)
            res.onSuccess {
                showMessage(it)
                selectEmployee(emp)
            }.onFailure {
                showMessage("فشل استرجاع النسخة الاحتياطية: ${it.localizedMessage}")
            }
        }
    }

    private fun recalculateForEmployee(emp: Employee) {
        val records = _uiState.value.dayRecords
        val (calcs, summary) = repository.calculateDays(emp, records)
        _uiState.value = _uiState.value.copy(
            dayCalculations = calcs,
            salarySummary = summary
        )
    }
}
