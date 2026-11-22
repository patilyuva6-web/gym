package com.example.gymapp.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    fun todayStartMillis(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun oneMonthFromTodayMillis(): Long {
        return LocalDate.now()
            .plusMonths(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun addOneMonth(epochMillis: Long): Long {
        val currentDue = millisToLocalDate(epochMillis)
        return currentDue
            .plusMonths(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun millisToLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun daysUntilDue(
        feeDueDateMillis: Long,
        today: LocalDate = LocalDate.now()
    ): Long {
        val dueDate = millisToLocalDate(feeDueDateMillis)
        return ChronoUnit.DAYS.between(today, dueDate)
    }

    fun formatDate(epochMillis: Long): String {
        return millisToLocalDate(epochMillis).format(displayFormatter)
    }

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        if (amount % 1.0 == 0.0) {
            formatter.maximumFractionDigits = 0
        } else {
            formatter.maximumFractionDigits = 2
        }
        return formatter.format(amount)
    }

    /**
     * Material 3 DatePicker returns midnight UTC for the picked date.
     * Convert it safely to local start-of-day milliseconds without shifting a day.
     */
    fun utcPickerMillisToLocalMillis(utcMillis: Long): Long {
        val pickedLocalDate = Instant.ofEpochMilli(utcMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return pickedLocalDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Converts stored local milliseconds back into midnight UTC for Material 3 DatePicker.
     */
    fun localMillisToUtcPickerMillis(localMillis: Long): Long {
        val localDate = millisToLocalDate(localMillis)
        return localDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
}
