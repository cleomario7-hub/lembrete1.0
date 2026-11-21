package com.example

import com.example.data.Reminder
import com.example.data.ReminderCategory
import com.example.data.ReminderPriority
import com.example.data.ReminderRecurrence
import com.example.data.ReminderTab
import com.example.ui.reminders.ReminderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun recurrence_advancesPastNow() {
        val now = 1_800_000_000_000L
        val pastDue = now - 3L * 24L * 60L * 60L * 1000L
        val nextDaily = ReminderRecurrence.DAILY.computeNextDueMillis(pastDue, now)
        assertTrue("Next daily occurrence should be strictly in the future", nextDaily > now)
    }

    @Test
    fun filterReminders_separatesTodayUpcomingAndCompleted() {
        val now = 1_800_000_000_000L
        val dueSoon = now + 60_000L
        val dueNextWeek = now + 7L * 24L * 60L * 60L * 1000L

        val reminders = listOf(
            Reminder(
                id = "1",
                title = "Pagar água",
                category = ReminderCategory.FINANCE.name,
                priority = ReminderPriority.HIGH.name,
                dueAtMillis = dueSoon,
                completed = false
            ),
            Reminder(
                id = "2",
                title = "Consulta médica",
                category = ReminderCategory.HEALTH.name,
                priority = ReminderPriority.MEDIUM.name,
                dueAtMillis = dueNextWeek,
                completed = false
            ),
            Reminder(
                id = "3",
                title = "Comprar café",
                category = ReminderCategory.SHOPPING.name,
                priority = ReminderPriority.LOW.name,
                dueAtMillis = dueSoon,
                completed = true
            )
        )

        val todayList = ReminderViewModel.filterReminders(
            reminders = reminders,
            tab = ReminderTab.TODAY,
            category = null,
            query = "",
            nowMillis = now
        )
        assertEquals(listOf("1"), todayList.map { it.id })

        val upcomingList = ReminderViewModel.filterReminders(
            reminders = reminders,
            tab = ReminderTab.UPCOMING,
            category = null,
            query = "",
            nowMillis = now
        )
        assertEquals(listOf("2"), upcomingList.map { it.id })

        val completedList = ReminderViewModel.filterReminders(
            reminders = reminders,
            tab = ReminderTab.COMPLETED,
            category = null,
            query = "",
            nowMillis = now
        )
        assertEquals(listOf("3"), completedList.map { it.id })
    }
}
