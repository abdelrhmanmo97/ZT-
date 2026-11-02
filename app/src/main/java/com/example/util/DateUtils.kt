package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val KEY_FORMAT = SimpleDateFormat("yyyy-M-d", Locale.US)
    private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.US)

    val ARABIC_DAYS = listOf(
        "الأحد",
        "الاثنين",
        "الثلاثاء",
        "الأربعاء",
        "الخميس",
        "الجمعة",
        "السبت"
    )

    fun todayIso(): String {
        return ISO_FORMAT.format(Date())
    }

    fun todayKey(): String {
        return dateToKey(Calendar.getInstance())
    }

    fun currentTimeHm(): String {
        return TIME_FORMAT.format(Date())
    }

    fun parseTimeToMinutes(timeStr: String?): Int? {
        if (timeStr.isNullOrBlank()) return null
        val parts = timeStr.trim().split(":")
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    fun minutesToHm(minutes: Int): String {
        val h = (minutes / 60) % 24
        val m = minutes % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    fun parseIsoToCalendar(iso: String): Calendar? {
        return try {
            val d = ISO_FORMAT.parse(iso.trim()) ?: return null
            Calendar.getInstance().apply { time = d }
        } catch (_: Exception) {
            null
        }
    }

    fun calendarToIso(cal: Calendar): String {
        return ISO_FORMAT.format(cal.time)
    }

    fun dateToKey(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    /**
     * Generates a sequence of Calendar days between startIso and endIso inclusive.
     * Capped at max 65 days.
     */
    fun getDatesBetween(startIso: String, endIso: String): List<Calendar> {
        val list = mutableListOf<Calendar>()
        val startCal = parseIsoToCalendar(startIso) ?: return list
        val endCal = parseIsoToCalendar(endIso) ?: return list

        if (startCal.after(endCal)) {
            // Swap if start is after end
            val tmp = startCal.time
            startCal.time = endCal.time
            endCal.time = tmp
        }

        val cur = startCal.clone() as Calendar
        // Reset time component
        cur.set(Calendar.HOUR_OF_DAY, 12)
        cur.set(Calendar.MINUTE, 0)
        cur.set(Calendar.SECOND, 0)
        cur.set(Calendar.MILLISECOND, 0)

        val end = endCal.clone() as Calendar
        end.set(Calendar.HOUR_OF_DAY, 12)
        end.set(Calendar.MINUTE, 0)
        end.set(Calendar.SECOND, 0)
        end.set(Calendar.MILLISECOND, 0)

        var count = 0
        while (!cur.after(end) && count < 65) {
            list.add(cur.clone() as Calendar)
            cur.add(Calendar.DAY_OF_MONTH, 1)
            count++
        }
        return list
    }

    /**
     * Shifts a date ISO string by n months, preserving day of month as close as possible.
     */
    fun shiftMonth(iso: String, months: Int): String {
        val cal = parseIsoToCalendar(iso) ?: return iso
        val originalDay = cal.get(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, months)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, minOf(originalDay, maxDays))
        return calendarToIso(cal)
    }

    fun getDayOfWeekArabic(cal: Calendar): String {
        // Calendar.SUNDAY is 1, ARABIC_DAYS[0] is Sunday
        val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return if (dayIndex in ARABIC_DAYS.indices) ARABIC_DAYS[dayIndex] else ""
    }

    fun isFriday(cal: Calendar): Boolean {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    fun getDayAndMonthFormatted(cal: Calendar): String {
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH) + 1
        return "$d/$m"
    }

    fun isToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    fun isBeforeToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val target = cal.clone() as Calendar
        target.set(Calendar.HOUR_OF_DAY, 0)
        target.set(Calendar.MINUTE, 0)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        return target.before(today)
    }

    fun getYesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return dateToKey(cal)
    }

    /**
     * Default date range: 28th of previous month to 27th of current month,
     * or standard current monthly cycle.
     */
    fun getDefaultDateRange(): Pair<String, String> {
        val today = Calendar.getInstance()
        val curDay = today.get(Calendar.DAY_OF_MONTH)
        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()

        if (curDay >= 28) {
            startCal.set(Calendar.DAY_OF_MONTH, 28)
            endCal.add(Calendar.MONTH, 1)
            endCal.set(Calendar.DAY_OF_MONTH, 27)
        } else {
            startCal.add(Calendar.MONTH, -1)
            startCal.set(Calendar.DAY_OF_MONTH, 28)
            endCal.set(Calendar.DAY_OF_MONTH, 27)
        }
        return Pair(calendarToIso(startCal), calendarToIso(endCal))
    }
}
