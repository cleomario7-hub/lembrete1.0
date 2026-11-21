package com.example.ui.reminders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Reminder
import com.example.data.ReminderCategory
import com.example.data.ReminderPriority
import com.example.data.ReminderRecurrence
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderEditorSheet(
    existingReminder: Reminder?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        notes: String,
        category: ReminderCategory,
        priority: ReminderPriority,
        recurrence: ReminderRecurrence,
        dueAtMillis: Long,
        notifyAlert: Boolean
    ) -> Unit,
    onDelete: ((Reminder) -> Unit)? = null
) {
    BackHandler(onBack = onDismiss)

    val defaultDueMillis = remember(existingReminder) {
        existingReminder?.dueAtMillis ?: (System.currentTimeMillis() + 60L * 60L * 1000L)
    }

    var title by rememberSaveable(existingReminder?.id) {
        mutableStateOf(existingReminder?.title.orEmpty())
    }
    var notes by rememberSaveable(existingReminder?.id) {
        mutableStateOf(existingReminder?.notes.orEmpty())
    }
    var categoryName by rememberSaveable(existingReminder?.id) {
        mutableStateOf((existingReminder?.categoryEnum ?: ReminderCategory.PERSONAL).name)
    }
    var priorityName by rememberSaveable(existingReminder?.id) {
        mutableStateOf((existingReminder?.priorityEnum ?: ReminderPriority.MEDIUM).name)
    }
    var recurrenceName by rememberSaveable(existingReminder?.id) {
        mutableStateOf((existingReminder?.recurrenceEnum ?: ReminderRecurrence.NONE).name)
    }
    var dueAtMillis by rememberSaveable(existingReminder?.id) {
        mutableLongStateOf(defaultDueMillis)
    }
    var notifyAlert by rememberSaveable(existingReminder?.id) {
        mutableStateOf(existingReminder?.notifyAlert ?: true)
    }
    var titleError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val selectedCategory = ReminderCategory.fromRaw(categoryName)
    val selectedPriority = ReminderPriority.fromRaw(priorityName)
    val selectedRecurrence = ReminderRecurrence.fromRaw(recurrenceName)

    val locale = Locale.forLanguageTag("pt-BR")
    val formattedDate = remember(dueAtMillis) {
        SimpleDateFormat("dd 'de' MMM, yyyy", locale).format(Date(dueAtMillis))
    }
    val formattedTime = remember(dueAtMillis) {
        SimpleDateFormat("HH:mm", locale).format(Date(dueAtMillis))
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (existingReminder == null) R.string.sheet_title_new else R.string.sheet_title_edit
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (existingReminder != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(existingReminder) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sheet_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_delete_reminder))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(140)
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text(stringResource(R.string.field_title_label)) },
                placeholder = { Text(stringResource(R.string.field_title_placeholder)) },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(stringResource(R.string.error_title_empty)) }
                } else null,
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it.take(1000) },
                label = { Text(stringResource(R.string.field_notes_label)) },
                placeholder = { Text(stringResource(R.string.field_notes_placeholder)) },
                minLines = 2,
                maxLines = 4,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_notes_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_quick_time),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { dueAtMillis = System.currentTimeMillis() + 30L * 60L * 1000L },
                    label = { Text(stringResource(R.string.preset_in_30m)) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("preset_30m_chip")
                )
                AssistChip(
                    onClick = { dueAtMillis = System.currentTimeMillis() + 2L * 60L * 60L * 1000L },
                    label = { Text(stringResource(R.string.preset_in_2h)) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("preset_2h_chip")
                )
                AssistChip(
                    onClick = {
                        dueAtMillis = Calendar.getInstance().apply {
                            if (get(Calendar.HOUR_OF_DAY) >= 20) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            set(Calendar.HOUR_OF_DAY, 20)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    },
                    label = { Text(stringResource(R.string.preset_tonight)) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("preset_tonight_chip")
                )
                AssistChip(
                    onClick = {
                        dueAtMillis = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    },
                    label = { Text(stringResource(R.string.preset_tomorrow_morning)) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("preset_tomorrow_chip")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.section_date_time),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp)
                        .testTag("pick_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formattedDate, style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .weight(0.85f)
                        .height(48.dp)
                        .testTag("pick_time_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = stringResource(R.string.pick_time),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formattedTime, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_category),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReminderCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { categoryName = cat.name },
                        label = { Text(stringResource(cat.labelRes)) },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sheet_cat_${cat.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.section_priority),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderPriority.entries.forEach { prio ->
                    FilterChip(
                        selected = selectedPriority == prio,
                        onClick = { priorityName = prio.name },
                        label = { Text(stringResource(prio.labelRes)) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sheet_prio_${prio.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.section_recurrence),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReminderRecurrence.entries.forEach { rec ->
                    FilterChip(
                        selected = selectedRecurrence == rec,
                        onClick = { recurrenceName = rec.name },
                        label = { Text(stringResource(rec.labelRes)) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sheet_rec_${rec.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.toggle_alert_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.toggle_alert_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notifyAlert,
                        onCheckedChange = { notifyAlert = it },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("notify_alert_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (title.trim().isEmpty()) {
                        titleError = true
                    } else {
                        onSave(
                            existingReminder?.id,
                            title,
                            notes,
                            selectedCategory,
                            selectedPriority,
                            selectedRecurrence,
                            dueAtMillis,
                            notifyAlert
                        )
                    }
                },
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_reminder_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (existingReminder == null) {
                                R.string.btn_save_reminder
                            } else {
                                R.string.btn_update_reminder
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueAtMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedUtc = datePickerState.selectedDateMillis
                        if (pickedUtc != null) {
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = pickedUtc
                            }
                            val localCal = Calendar.getInstance().apply {
                                timeInMillis = dueAtMillis
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            }
                            dueAtMillis = localCal.timeInMillis
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val currentCal = remember(dueAtMillis) {
            Calendar.getInstance().apply { timeInMillis = dueAtMillis }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentCal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.pick_time)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedCal = Calendar.getInstance().apply {
                            timeInMillis = dueAtMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        dueAtMillis = updatedCal.timeInMillis
                        showTimePicker = false
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
