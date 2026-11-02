package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "محمد أحمد",
    val salary: Double = 6000.0,
    val divisorDays: Double = 30.0,
    val startTime: String = "09:00",
    val endTime: String = "17:00",
    val overtimeMultiplier: Double = 1.5,
    val mealValue: Double = 20.0,
    val mealThresholdTime: String = "22:00",
    val productionBonus: Double = 0.0,
    val loanRepayment: Double = 0.0,
    val personalPrint: Double = 0.0,
    val delayDeductionPerMin: Double = 0.0,
    val absenceMultiplier: Double = 1.0,
    val startDate: String = "2026-09-28",
    val endDate: String = "2026-10-27",
    val holidaysCsv: String = "",
    val accentColorHex: String = "#8B5CF6",
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
    val userRole: String = "employee"
)

@Entity(
    tableName = "day_records",
    indices = [
        Index(value = ["employeeId", "dateKey"], unique = true)
    ]
)
data class DayRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val dateKey: String,
    val inTime: String? = null,
    val outTime: String? = null,
    val breakStart: String? = null,
    val breakEnd: String? = null,
    val status: String = "",
    val mealSetting: String = "",
    val note: String = "",
    val punchLat: Double? = null,
    val punchLng: Double? = null,
    val withinGeofence: Boolean = true,
    val syncStatus: String = "synced"
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val typeArabic: String,
    val startDate: String,
    val endDate: String,
    val durationText: String,
    val reason: String,
    val status: String = "pending",
    val createdAt: String
)

@Entity(tableName = "correction_requests")
data class CorrectionRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val dateKey: String,
    val requestedIn: String?,
    val requestedOut: String?,
    val reason: String,
    val status: String = "pending",
    val createdAt: String
)

@Entity(
    tableName = "user_accounts",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["employeeId"], unique = true)
    ]
)
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val fullName: String,
    val employeeCode: String,
    val resetCode: String? = null,
    val createdAt: String
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val title: String,
    val message: String,
    val type: String = "system",
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "hr_tickets")
data class HrTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val category: String,
    val subject: String,
    val message: String,
    val priority: String = "عادي",
    val status: String = "pending",
    val adminResponse: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Dao
interface SalaryDao {
    @Query("SELECT * FROM employees ORDER BY id ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees")
    suspend fun getAllEmployeesSync(): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("SELECT * FROM day_records WHERE employeeId = :employeeId")
    fun getDayRecordsForEmployee(employeeId: Long): Flow<List<DayRecordEntity>>

    @Query("SELECT * FROM day_records WHERE employeeId = :employeeId")
    suspend fun getDayRecordsForEmployeeSync(employeeId: Long): List<DayRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDayRecord(dayRecord: DayRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDayRecords(dayRecords: List<DayRecordEntity>)

    @Query("DELETE FROM day_records WHERE employeeId = :employeeId")
    suspend fun clearRecordsForEmployee(employeeId: Long)

    @Query("DELETE FROM day_records WHERE employeeId = :employeeId AND dateKey = :dateKey")
    suspend fun deleteRecordForDate(employeeId: Long, dateKey: String)

    @Query("SELECT * FROM day_records WHERE employeeId = :employeeId AND syncStatus = 'pending_sync'")
    suspend fun getPendingSyncRecords(employeeId: Long): List<DayRecordEntity>

    @Query("UPDATE day_records SET syncStatus = 'synced' WHERE id IN (:ids)")
    suspend fun markRecordsAsSynced(ids: List<Long>)

    // Leaves
    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getLeaveRequests(employeeId: Long): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity): Long

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequestEntity)

    // Corrections
    @Query("SELECT * FROM correction_requests WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getCorrectionRequests(employeeId: Long): Flow<List<CorrectionRequestEntity>>

    @Query("SELECT * FROM correction_requests ORDER BY id DESC")
    fun getAllCorrectionRequests(): Flow<List<CorrectionRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrectionRequest(request: CorrectionRequestEntity): Long

    @Update
    suspend fun updateCorrectionRequest(request: CorrectionRequestEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getNotificationsForEmployee(employeeId: Long): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE employeeId = :employeeId")
    suspend fun markAllNotificationsAsRead(employeeId: Long)

    @Query("DELETE FROM notifications WHERE employeeId = :employeeId")
    suspend fun clearNotificationsForEmployee(employeeId: Long)

    // HR Tickets / Inquiries
    @Query("SELECT * FROM hr_tickets WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getTicketsForEmployee(employeeId: Long): Flow<List<HrTicketEntity>>

    @Query("SELECT * FROM hr_tickets ORDER BY id DESC")
    fun getAllTickets(): Flow<List<HrTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: HrTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: HrTicketEntity)

    @Query("DELETE FROM hr_tickets WHERE id = :id")
    suspend fun deleteTicket(id: Long)

    // User Accounts (Authentication)
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE employeeId = :employeeId LIMIT 1")
    suspend fun getUserByEmployeeId(employeeId: Long): UserAccountEntity?

    @Query("SELECT * FROM user_accounts")
    suspend fun getAllUsers(): List<UserAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)
}

@Database(
    entities = [
        EmployeeEntity::class,
        DayRecordEntity::class,
        LeaveRequestEntity::class,
        CorrectionRequestEntity::class,
        UserAccountEntity::class,
        AppNotificationEntity::class,
        HrTicketEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SalaryDatabase : RoomDatabase() {
    abstract fun salaryDao(): SalaryDao
}
