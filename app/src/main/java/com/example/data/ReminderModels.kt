package com.example.data

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.example.R
import java.util.Calendar

@Keep
data class Reminder(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val notes: String = "",
    val category: String = ReminderCategory.PERSONAL.name,
    val priority: String = ReminderPriority.MEDIUM.name,
    val recurrence: String = ReminderRecurrence.NONE.name,
    val dueAtMillis: Long = 0L,
    val completed: Boolean = false,
    val notifyAlert: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val categoryEnum: ReminderCategory
        get() = ReminderCategory.fromRaw(category)

    val priorityEnum: ReminderPriority
        get() = ReminderPriority.fromRaw(priority)

    val recurrenceEnum: ReminderRecurrence
        get() = ReminderRecurrence.fromRaw(recurrence)
}

enum class ReminderCategory(@param:StringRes val labelRes: Int) {
    PERSONAL(R.string.cat_personal),
    WORK(R.string.cat_work),
    HEALTH(R.string.cat_health),
    FINANCE(R.string.cat_finance),
    STUDY(R.string.cat_study),
    SHOPPING(R.string.cat_shopping);

    companion object {
        fun fromRaw(raw: String?): ReminderCategory =
            entries.firstOrNull { it.name == raw } ?: PERSONAL
    }
}

enum class ReminderPriority(@param:StringRes val labelRes: Int, val weight: Int) {
    HIGH(R.string.prio_high, 3),
    MEDIUM(R.string.prio_medium, 2),
    LOW(R.string.prio_low, 1);

    companion object {
        fun fromRaw(raw: String?): ReminderPriority =
            entries.firstOrNull { it.name == raw } ?: MEDIUM
    }
}

enum class ReminderRecurrence(@param:StringRes val labelRes: Int) {
    NONE(R.string.rec_none),
    DAILY(R.string.rec_daily),
    WEEKLY(R.string.rec_weekly),
    MONTHLY(R.string.rec_monthly);

    fun computeNextDueMillis(currentDueMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long {
        if (this == NONE) return currentDueMillis
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentDueMillis.coerceAtLeast(1L)
        }
        // Advance at least once, and keep advancing until strictly in the future relative to nowMillis
        do {
            when (this) {
                DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                MONTHLY -> cal.add(Calendar.MONTH, 1)
                NONE -> Unit
            }
        } while (cal.timeInMillis <= nowMillis)
        return cal.timeInMillis
    }

    companion object {
        fun fromRaw(raw: String?): ReminderRecurrence =
            entries.firstOrNull { it.name == raw } ?: NONE
    }
}

enum class ReminderTab(@param:StringRes val labelRes: Int) {
    TODAY(R.string.tab_today),
    UPCOMING(R.string.tab_upcoming),
    ALL(R.string.tab_all),
    COMPLETED(R.string.tab_completed)
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
