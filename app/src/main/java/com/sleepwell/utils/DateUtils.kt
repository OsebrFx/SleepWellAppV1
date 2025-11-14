package com.sleepwell.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getCurrentDate(): Date = Date()

    fun getDateDaysAgo(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.time
    }

    fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun getStartOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun getDatesBetween(startDate: Date, endDate: Date): List<Date> {
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (calendar.time <= endDate) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dates
    }

    fun getLastNDays(n: Int): List<Date> {
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance()

        for (i in (n - 1) downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            dates.add(calendar.time)
        }

        return dates
    }

    fun calculateDaysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun formatTimeRange(startTime: Date, endTime: Date): String {
        val timeFormat = SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
        return "${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
    }

    fun parseDateFromString(dateString: String, format: String = Constants.DATE_FORMAT_DISPLAY): Date? {
        return try {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun isToday(date: Date): Boolean {
        return date.isSameDay(Date())
    }

    fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return date.isSameDay(yesterday.time)
    }

    fun getDayOfWeek(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return dayFormat.format(date)
    }

    fun getShortDayOfWeek(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return dayFormat.format(date)
    }

    fun createTime(hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getHourFromDate(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    fun getMinuteFromDate(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MINUTE)
    }

    fun calculateSleepDuration(startTime: Date, endTime: Date): Float {
        val durationMillis = endTime.time - startTime.time
        return durationMillis.millisToHours()
    }
}
