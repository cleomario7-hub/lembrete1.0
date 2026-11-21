package com.example.ui.reminders

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.Reminder
import com.example.data.ReminderCategory
import com.example.data.ReminderPriority
import com.example.data.ReminderRecurrence
import com.example.data.ReminderRepository
import com.example.data.ReminderTab
import com.example.data.UiState
import com.example.notifications.ReminderNotificationScheduler
import java.util.Calendar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SnackbarMessage {
    data class Resource(@param:StringRes val resId: Int) : SnackbarMessage
    data class Dynamic(val text: String) : SnackbarMessage
}

data class ReminderSummaryCounts(
    val dueTodayCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0
)

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val scheduler: ReminderNotificationScheduler,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ReminderTab.TODAY)
    val selectedTab: StateFlow<ReminderTab> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ReminderCategory?>(null)
    val selectedCategory: StateFlow<ReminderCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    private val _editingReminder = MutableStateFlow<Reminder?>(null)
    val editingReminder: StateFlow<Reminder?> = _editingReminder.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<SnackbarMessage>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<SnackbarMessage> = _snackbarMessages.asSharedFlow()

    val remindersState: StateFlow<UiState<List<Reminder>>> = repository
        .observeReminders(currentUserId)
        .onEach { items ->
            scheduler.syncAlarms(items)
        }
        .map<List<Reminder>, UiState<List<Reminder>>> { items ->
            UiState.Success(items)
        }
        .catch { error ->
            if (error is CancellationException) throw error
            val rootError = error
            Log.w(TAG, "Error observing reminders", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Erro ao carregar lembretes."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun selectTab(tab: ReminderTab) {
        _selectedTab.value = tab
    }

    fun selectCategory(category: ReminderCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openNewReminderSheet() {
        _editingReminder.value = null
        _isEditorOpen.value = true
    }

    fun openEditReminderSheet(reminder: Reminder) {
        _editingReminder.value = reminder
        _isEditorOpen.value = true
    }

    fun dismissEditorSheet() {
        _isEditorOpen.value = false
        _editingReminder.value = null
    }

    fun saveReminder(
        existingId: String?,
        title: String,
        notes: String,
        category: ReminderCategory,
        priority: ReminderPriority,
        recurrence: ReminderRecurrence,
        dueAtMillis: Long,
        notifyAlert: Boolean
    ) {
        val trimmedTitle = title.trim().take(140)
        if (trimmedTitle.isEmpty()) {
            _snackbarMessages.tryEmit(SnackbarMessage.Resource(R.string.error_title_empty))
            return
        }
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            val result = if (existingId.isNullOrBlank()) {
                repository.addReminder(
                    userId = currentUserId,
                    title = trimmedTitle,
                    notes = notes.trim().take(1000),
                    category = category,
                    priority = priority,
                    recurrence = recurrence,
                    dueAtMillis = dueAtMillis,
                    notifyAlert = notifyAlert
                ).map { newId ->
                    if (notifyAlert && dueAtMillis > System.currentTimeMillis()) {
                        scheduler.scheduleReminderAlarm(
                            reminderId = newId,
                            title = trimmedTitle,
                            notes = notes.trim().take(1000),
                            dueAtMillis = dueAtMillis
                        )
                    }
                }
            } else {
                repository.updateReminder(
                    userId = currentUserId,
                    reminderId = existingId,
                    title = trimmedTitle,
                    notes = notes.trim().take(1000),
                    category = category,
                    priority = priority,
                    recurrence = recurrence,
                    dueAtMillis = dueAtMillis,
                    notifyAlert = notifyAlert
                ).onSuccess {
                    if (notifyAlert && dueAtMillis > System.currentTimeMillis()) {
                        scheduler.scheduleReminderAlarm(
                            reminderId = existingId,
                            title = trimmedTitle,
                            notes = notes.trim().take(1000),
                            dueAtMillis = dueAtMillis
                        )
                    } else {
                        scheduler.cancelReminderAlarm(existingId)
                    }
                }
            }

            _isSaving.value = false
            result.fold(
                onSuccess = {
                    dismissEditorSheet()
                    _snackbarMessages.tryEmit(SnackbarMessage.Resource(R.string.msg_reminder_saved))
                },
                onFailure = { err ->
                    _snackbarMessages.tryEmit(
                        err.localizedMessage?.let { SnackbarMessage.Dynamic(it) }
                            ?: SnackbarMessage.Resource(R.string.error_generic)
                    )
                }
            )
        }
    }

    fun toggleComplete(reminder: Reminder) {
        if (reminder.id.isBlank()) return
        viewModelScope.launch {
            // If marking an active recurring reminder as done, advance it to the next cycle!
            val recurrence = reminder.recurrenceEnum
            if (!reminder.completed && recurrence != ReminderRecurrence.NONE) {
                val nextDue = recurrence.computeNextDueMillis(reminder.dueAtMillis)
                repository.rescheduleReminder(
                    userId = currentUserId,
                    reminderId = reminder.id,
                    newDueAtMillis = nextDue,
                    completed = false
                ).fold(
                    onSuccess = {
                        if (reminder.notifyAlert) {
                            scheduler.scheduleReminderAlarm(
                                reminderId = reminder.id,
                                title = reminder.title,
                                notes = reminder.notes,
                                dueAtMillis = nextDue
                            )
                        }
                        _snackbarMessages.tryEmit(
                            SnackbarMessage.Resource(R.string.msg_recurring_advanced)
                        )
                    },
                    onFailure = {
                        _snackbarMessages.tryEmit(SnackbarMessage.Resource(R.string.error_generic))
                    }
                )
            } else {
                val targetCompleted = !reminder.completed
                repository.setCompleted(
                    userId = currentUserId,
                    reminderId = reminder.id,
                    completed = targetCompleted
                ).fold(
                    onSuccess = {
                        if (targetCompleted) {
                            scheduler.cancelReminderAlarm(reminder.id)
                        } else if (reminder.notifyAlert && reminder.dueAtMillis > System.currentTimeMillis()) {
                            scheduler.scheduleReminderAlarm(
                                reminderId = reminder.id,
                                title = reminder.title,
                                notes = reminder.notes,
                                dueAtMillis = reminder.dueAtMillis
                            )
                        }
                    },
                    onFailure = {
                        _snackbarMessages.tryEmit(SnackbarMessage.Resource(R.string.error_generic))
                    }
                )
            }
        }
    }

    fun snoozeOneHour(reminder: Reminder) {
        if (reminder.id.isBlank()) return
        val baseTime = maxOf(System.currentTimeMillis(), reminder.dueAtMillis)
        val newDue = baseTime + 60L * 60L * 1000L
        rescheduleTo(reminder, newDue)
    }

    fun snoozeTomorrowMorning(reminder: Reminder) {
        if (reminder.id.isBlank()) return
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        rescheduleTo(reminder, cal.timeInMillis)
    }

    private fun rescheduleTo(reminder: Reminder, newDueMillis: Long) {
        viewModelScope.launch {
            repository.rescheduleReminder(
                userId = currentUserId,
                reminderId = reminder.id,
                newDueAtMillis = newDueMillis,
                completed = false
            ).fold(
                onSuccess = {
                    if (reminder.notifyAlert) {
                        scheduler.scheduleReminderAlarm(
                            reminderId = reminder.id,
                            title = reminder.title,
                            notes = reminder.notes,
                            dueAtMillis = newDueMillis
                        )
                    }
                    _snackbarMessages.tryEmit(
                        SnackbarMessage.Resource(R.string.msg_reminder_snoozed)
                    )
                },
                onFailure = {
                    _snackbarMessages.tryEmit(SnackbarMessage.Resource(R.string.error_generic))
                }
            )
        }
    }

    fun deleteReminder(reminder: Reminder) {
        if (reminder.id.isBlank()) return
        viewModelScope.launch {
            scheduler.cancelReminderAlarm(reminder.id)
            repository.deleteReminder(currentUserId, reminder.id).fold(
                onSuccess = {
                    if (_editingReminder.value?.id == reminder.id) {
                        dismissEditorSheet()
                    }
                    _snackbarMessages.tryEmit(
                        SnackbarMessage.Resource(R.string.msg_reminder_deleted)
                    )
                },
                onFailure = {
                    _snackbarMessages.tryEmit(SnackbarMessage.Resource(R.string.error_generic))
                }
            )
        }
    }

    companion object {
        private const val TAG = "ReminderVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun endOfTodayMillis(nowMillis: Long = System.currentTimeMillis()): Long {
            return Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        }

        fun computeSummary(
            reminders: List<Reminder>,
            nowMillis: Long = System.currentTimeMillis()
        ): ReminderSummaryCounts {
            val endToday = endOfTodayMillis(nowMillis)
            var dueToday = 0
            var overdue = 0
            var completed = 0
            reminders.forEach { rem ->
                if (rem.completed) {
                    completed++
                } else {
                    if (rem.dueAtMillis < nowMillis) {
                        overdue++
                    }
                    if (rem.dueAtMillis <= endToday) {
                        dueToday++
                    }
                }
            }
            return ReminderSummaryCounts(
                dueTodayCount = dueToday,
                overdueCount = overdue,
                completedCount = completed
            )
        }

        fun filterReminders(
            reminders: List<Reminder>,
            tab: ReminderTab,
            category: ReminderCategory?,
            query: String,
            nowMillis: Long = System.currentTimeMillis()
        ): List<Reminder> {
            val endToday = endOfTodayMillis(nowMillis)
            val trimmedQuery = query.trim().lowercase()

            val filtered = reminders.filter { rem ->
                val matchesTab = when (tab) {
                    ReminderTab.TODAY -> !rem.completed && rem.dueAtMillis <= endToday
                    ReminderTab.UPCOMING -> !rem.completed && rem.dueAtMillis > endToday
                    ReminderTab.ALL -> !rem.completed
                    ReminderTab.COMPLETED -> rem.completed
                }
                val matchesCategory = category == null || rem.categoryEnum == category
                val matchesQuery = trimmedQuery.isEmpty() ||
                    rem.title.lowercase().contains(trimmedQuery) ||
                    rem.notes.lowercase().contains(trimmedQuery)

                matchesTab && matchesCategory && matchesQuery
            }

            return if (tab == ReminderTab.COMPLETED) {
                filtered.sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
            } else {
                filtered.sortedWith(
                    compareBy<Reminder> { it.dueAtMillis }
                        .thenByDescending { it.priorityEnum.weight }
                )
            }
        }
    }
}
