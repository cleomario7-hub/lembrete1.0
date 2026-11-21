package com.example.ui.reminders

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Reminder
import com.example.data.ReminderRecurrence
import com.example.ui.theme.LocalExtendedColors
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggleComplete: () -> Unit,
    onCardClick: () -> Unit,
    onSnoozeOneHour: () -> Unit,
    onSnoozeTomorrow: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalExtendedColors.current
    val nowMillis = System.currentTimeMillis()
    val isOverdue = !reminder.completed && reminder.dueAtMillis < nowMillis
    val categoryEnum = reminder.categoryEnum
    val priorityEnum = reminder.priorityEnum
    val recurrenceEnum = reminder.recurrenceEnum

    val accentStripeColor by animateColorAsState(
        targetValue = when {
            reminder.completed -> ext.successAccent.copy(alpha = 0.55f)
            isOverdue -> ext.priorityHigh
            else -> priorityEnum.accentColor()
        },
        label = "priorityStripeColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("reminder_card_${reminder.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.completed) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOverdue) {
                ext.priorityHigh.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (reminder.completed) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Priority / status accent bar on leading edge
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentStripeColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reminder_checkbox_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = if (reminder.completed) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = stringResource(
                                if (reminder.completed) {
                                    R.string.mark_incomplete_desc
                                } else {
                                    R.string.mark_complete_desc
                                }
                            ),
                            tint = if (reminder.completed) {
                                ext.successAccent
                            } else {
                                priorityEnum.accentColor()
                            },
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                textDecoration = if (reminder.completed) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                }
                            ),
                            color = if (reminder.completed) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (reminder.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reminder.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reminder_delete_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_reminder_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata badges row
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DueTimeBadge(
                        dueAtMillis = reminder.dueAtMillis,
                        isCompleted = reminder.completed,
                        isOverdue = isOverdue
                    )

                    CategoryBadge(
                        label = stringResource(categoryEnum.labelRes),
                        icon = categoryEnum.icon(),
                        tint = categoryEnum.accentColor()
                    )

                    PriorityBadge(
                        label = stringResource(priorityEnum.labelRes),
                        tint = priorityEnum.accentColor(),
                        container = priorityEnum.containerColor()
                    )

                    if (recurrenceEnum != ReminderRecurrence.NONE) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(recurrenceEnum.labelRes),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    if (reminder.notifyAlert && !reminder.completed) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .size(13.dp)
                            )
                        }
                    }
                }

                // Quick 1-tap snooze actions when overdue or due soon
                if (!reminder.completed && reminder.dueAtMillis <= ReminderViewModel.endOfTodayMillis(nowMillis)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SnoozeActionChip(
                            label = stringResource(R.string.snooze_1h),
                            onClick = onSnoozeOneHour,
                            testTag = "snooze_1h_${reminder.id}"
                        )
                        SnoozeActionChip(
                            label = stringResource(R.string.snooze_tomorrow),
                            onClick = onSnoozeTomorrow,
                            testTag = "snooze_tomorrow_${reminder.id}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DueTimeBadge(
    dueAtMillis: Long,
    isCompleted: Boolean,
    isOverdue: Boolean
) {
    val ext = LocalExtendedColors.current
    val now = Calendar.getInstance()
    val dueCal = Calendar.getInstance().apply { timeInMillis = dueAtMillis }

    val isSameDayToday = now.get(Calendar.YEAR) == dueCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == dueCal.get(Calendar.DAY_OF_YEAR)

    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = tomorrowCal.get(Calendar.YEAR) == dueCal.get(Calendar.YEAR) &&
        tomorrowCal.get(Calendar.DAY_OF_YEAR) == dueCal.get(Calendar.DAY_OF_YEAR)

    val prefix = when {
        isOverdue -> "${stringResource(R.string.badge_overdue)} • "
        isSameDayToday -> "${stringResource(R.string.badge_today)} • "
        isTomorrow -> "${stringResource(R.string.badge_tomorrow)} • "
        else -> ""
    }

    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.surfaceVariant
        isOverdue -> ext.priorityHighContainer
        isSameDayToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
        isOverdue -> ext.priorityHigh
        isSameDayToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$prefix${formatReminderDateTime(dueAtMillis)}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CategoryBadge(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        contentColor = tint,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun PriorityBadge(
    label: String,
    tint: Color,
    container: Color
) {
    Surface(
        color = container,
        contentColor = tint,
        shape = CircleShape
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SnoozeActionChip(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Snooze,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    )
}
