package com.example.core


import java.text.SimpleDateFormat
import java.util.*
object DateUtils {

    private const val STORAGE_FORMAT = "yyyyMMddHHmmss"
    private const val UI_FORMAT = "dd/MM/yyyy"

    private val storageFormatter by lazy { SimpleDateFormat(STORAGE_FORMAT, Locale.US) }
    private val uiFormatter by lazy { SimpleDateFormat(UI_FORMAT, Locale.getDefault()) }

    // ---------------- UI -> STORAGE ----------------
    fun uiToStorage(input: String): String? {
        return try {
            val date = uiFormatter.parse(input) ?: return null

            val calendar = Calendar.getInstance()
            calendar.time = date

            // force ώρα = 00:00:00
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            storageFormatter.format(calendar.time)
        } catch (e: Exception) {
            null
        }
    }

    // ---------------- STORAGE -> UI ----------------
    fun storageToUi(value: String?): String? {
        return try {
            val date = storageFormatter.parse(value) ?: return null
            uiFormatter.format(date)
        } catch (e: Exception) {
            null
        }
    }

    // ---------------- NOW (for audit fields) ----------------
    fun nowStorage(): String {
        return storageFormatter.format(Date())
    }
}

