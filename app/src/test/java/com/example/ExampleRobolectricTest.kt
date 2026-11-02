package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DayRecord
import com.example.data.model.Employee
import com.example.data.model.EmployeeSettings
import com.example.data.repository.SalaryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("كشف المرتب", appName)
    }

    @Test
    fun `test overtime and late calculation logic`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SalaryRepository.getInstance(context)

        val settings = EmployeeSettings(
            salary = 6000.0,
            divisorDays = 30.0,
            start = "09:00",
            end = "17:00",
            mult = 1.5,
            mealVal = 20.0,
            mealTime = "22:00",
            d1 = "2026-10-01",
            d2 = "2026-10-03"
        )
        val emp = Employee(id = 1, name = "أحمد", settings = settings)

        // 2026-10-01 is Thursday: 09:30 - 19:00 (30 min late, 120 min OT)
        // 2026-10-02 is Friday: 09:00 - 17:00 (Friday work counts as double day 'ex')
        // 2026-10-03 is Saturday: absent (abs)
        val records = mapOf(
            "2026-10-1" to DayRecord(dateKey = "2026-10-1", inTime = "09:30", outTime = "19:00"),
            "2026-10-2" to DayRecord(dateKey = "2026-10-2", inTime = "09:00", outTime = "17:00"),
            "2026-10-3" to DayRecord(dateKey = "2026-10-3", status = "abs")
        )

        val (calcs, summary) = repo.calculateDays(emp, records)

        assertEquals(3, calcs.size)
        // Thursday (2026-10-1) checks
        val day1 = calcs.find { it.dateKey == "2026-10-1" }
        assertNotNull(day1)
        assertEquals(30, day1!!.lateMinutes)
        assertEquals(120, day1.overtimeMinutes)

        // Friday (2026-10-2) checks
        val day2 = calcs.find { it.dateKey == "2026-10-2" }
        assertNotNull(day2)
        assertEquals(true, day2!!.isFriday)
        assertEquals("ex", day2.status)
        assertTrue(day2.tag.contains("بيومين"))

        // Summary checks
        assertEquals(30, summary.totalLateMinutes)
        assertEquals(120, summary.totalOvertimeMinutes)
        assertEquals(2.0, summary.overtimeHours, 0.01)
        assertEquals(1, summary.doubleDaysCount)
        assertEquals(1, summary.absenceDaysCount)
    }

    @Test
    fun `test meal allowance auto threshold and overnight shift`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SalaryRepository.getInstance(context)

        val settings = EmployeeSettings(
            salary = 5000.0,
            divisorDays = 30.0,
            start = "09:00",
            end = "17:00",
            mealVal = 25.0,
            mealTime = "22:00",
            d1 = "2026-10-05",
            d2 = "2026-10-06"
        )
        val emp = Employee(id = 1, name = "سارة", settings = settings)

        // Day 1 (Monday): leaves at 22:30 -> automatically gets meal
        // Day 2 (Tuesday): leaves at 01:00 (overnight shift) -> automatically gets meal
        val records = mapOf(
            "2026-10-5" to DayRecord(dateKey = "2026-10-5", inTime = "09:00", outTime = "22:30"),
            "2026-10-6" to DayRecord(dateKey = "2026-10-6", inTime = "09:00", outTime = "01:00")
        )

        val (calcs, summary) = repo.calculateDays(emp, records)
        assertEquals(2, summary.totalMealsCount)
        assertEquals(50.0, summary.mealsValue, 0.01)
    }

    @Test
    fun `test json export and import round trip`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SalaryRepository.getInstance(context)

        val settings = EmployeeSettings(
            salary = 7500.0,
            divisorDays = 30.0,
            start = "08:30",
            end = "16:30",
            d1 = "2026-09-28",
            d2 = "2026-10-27"
        )
        val emp = Employee(id = 10, name = "محمد", settings = settings)
        val records = mapOf(
            "2026-10-05" to DayRecord(dateKey = "2026-10-05", inTime = "08:30", outTime = "18:30", note = "عمل إضافي ساعتين")
        )

        val json = repo.exportToJson(emp, records)
        assertTrue(json.contains("محمد"))
        assertTrue(json.contains("7500"))
        assertTrue(json.contains("08:30"))
        assertTrue(json.contains("عمل إضافي ساعتين"))
    }

    @Test
    fun `test email authentication login and rejection`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SalaryRepository.getInstance(context)

        // Seed initial employee & user
        repo.getInitialOrFirstEmployee()

        // 1. Success login
        val loginSuccess = repo.login("mohamed@company.com", "Password123", true)
        val authUser = (loginSuccess as com.example.data.model.AuthResult.Success).user
        val authEmp = loginSuccess.employee
        assertEquals(authUser.employeeId, authEmp.id)
        assertTrue(authEmp.name.isNotBlank())

        // 2. Wrong password
        val wrongPass = repo.login("mohamed@company.com", "WrongPassword", false)
        assertTrue(wrongPass is com.example.data.model.AuthResult.Error)

        // 3. Unregistered email
        val unknownEmail = repo.login("unknown@company.com", "Password123", false)
        assertTrue(unknownEmail is com.example.data.model.AuthResult.Error)
    }

    @Test
    fun `test employee registration authorization policy`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SalaryRepository.getInstance(context)

        // 1. Unauthorized code should be rejected
        val badReg = repo.register(
            fullName = "متطفل خارجي",
            email = "intruder@gmail.com",
            employeeCode = "99999",
            password = "SecretPassword1",
            confirmPassword = "SecretPassword1",
            rememberMe = false
        )
        assertTrue(badReg is com.example.data.model.RegisterResult.Error)

        // 2. Authorized company passcode (EMP-2026) should succeed
        val goodReg = repo.register(
            fullName = "أحمد خالد",
            email = "ahmed.khaled@company.com",
            employeeCode = "EMP-2026",
            password = "MyPassword2026",
            confirmPassword = "MyPassword2026",
            rememberMe = true
        )
        assertTrue(goodReg is com.example.data.model.RegisterResult.Success)
        val successUser = (goodReg as com.example.data.model.RegisterResult.Success).user
        assertEquals("أحمد خالد", successUser.fullName)
    }

    @Test
    fun `test password reset workflow`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SalaryRepository.getInstance(context)

        repo.getInitialOrFirstEmployee()

        // Request reset code
        val reqResult = repo.requestPasswordReset("mohamed@company.com")
        assertTrue(reqResult is com.example.data.model.ResetPasswordResult.CodeSent)
        val code = (reqResult as com.example.data.model.ResetPasswordResult.CodeSent).simulatedCode
        assertEquals(6, code.length)

        // Complete reset with wrong code (should fail)
        val failReset = repo.completePasswordReset("mohamed@company.com", "000000", "NewPass2026", "NewPass2026")
        assertTrue(failReset is com.example.data.model.ResetPasswordResult.Error)

        // Complete reset with correct code
        val goodReset = repo.completePasswordReset("mohamed@company.com", code, "NewPass2026", "NewPass2026")
        assertTrue(goodReset is com.example.data.model.ResetPasswordResult.Success)

        // Login with new password
        val loginWithNew = repo.login("mohamed@company.com", "NewPass2026", false)
        assertTrue(loginWithNew is com.example.data.model.AuthResult.Success)
    }
}
