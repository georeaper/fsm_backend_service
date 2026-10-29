package com.example.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val STORAGE_FORMAT = "yyyyMMddHHmmss"
    private const val UI_FORMAT = "dd/MM/yyyy"
    private const val UI_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss"

    fun uiToStorage(input: String): String? {
        val normalized = input.trim()
        val pattern = if (normalized.contains(' ')) UI_DATE_TIME_FORMAT else UI_FORMAT
        val parsed = parse(normalized, pattern) ?: return null
        return format(parsed, STORAGE_FORMAT)
    }

    fun storageToUi(value: String?): String? {
        val normalized = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val parsed = parse(normalized, STORAGE_FORMAT)
            ?: parse(normalized, UI_DATE_TIME_FORMAT)
            ?: parse(normalized, UI_FORMAT)
            ?: return null
        return format(parsed, UI_FORMAT)
    }

    fun nowStorage(): String = format(Date(), STORAGE_FORMAT)

    fun normalizeDateTimeStorage(value: String?): String? {
        val normalized = value?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val parsed = parse(normalized, STORAGE_FORMAT)
            ?: parse(normalized, UI_DATE_TIME_FORMAT)
            ?: return null
        return format(parsed, STORAGE_FORMAT)
    }

    fun durationHours(start: String?, end: String?): Double {
        val normalizedStart = normalizeDateTimeStorage(start) ?: return 0.0
        val normalizedEnd = normalizeDateTimeStorage(end) ?: return 0.0
        val startDate = parse(normalizedStart, STORAGE_FORMAT) ?: return 0.0
        val endDate = parse(normalizedEnd, STORAGE_FORMAT) ?: return 0.0
        return ((endDate.time - startDate.time).coerceAtLeast(0L) / 3_600_000.0)
    }

    fun rangeDays(start: String?, end: String?): Long? {
        val normalizedStart = normalizeDateTimeStorage(start) ?: return null
        val normalizedEnd = normalizeDateTimeStorage(end) ?: return null
        val startDate = parse(normalizedStart, STORAGE_FORMAT) ?: return null
        val endDate = parse(normalizedEnd, STORAGE_FORMAT) ?: return null
        return (endDate.time - startDate.time) / 86_400_000L
    }

    private fun parse(value: String, pattern: String): Date? = runCatching {
        SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(value)
    }.getOrNull()

    private fun format(value: Date, pattern: String): String =
        SimpleDateFormat(pattern, Locale.US).format(value)
}
