package com.example.ui.reminders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ReminderCategory
import com.example.data.ReminderPriority
import com.example.data.ReminderTab
import com.example.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun ReminderCategory.icon(): ImageVector = when (this) {
    ReminderCategory.PERSONAL -> Icons.Filled.Person
    ReminderCategory.WORK -> Icons.Filled.Work
    ReminderCategory.HEALTH -> Icons.Filled.Favorite
    ReminderCategory.FINANCE -> Icons.Filled.AccountBalanceWallet
    ReminderCategory.STUDY -> Icons.Filled.School
    ReminderCategory.SHOPPING -> Icons.Filled.ShoppingBag
}

@Composable
fun ReminderCategory.accentColor(): Color {
    val ext = LocalExtendedColors.current
    return when (this) {
        ReminderCategory.PERSONAL -> ext.categoryPersonal
        ReminderCategory.WORK -> ext.categoryWork
        ReminderCategory.HEALTH -> ext.categoryHealth
        ReminderCategory.FINANCE -> ext.categoryFinance
        ReminderCategory.STUDY -> ext.categoryStudy
        ReminderCategory.SHOPPING -> ext.categoryShopping
    }
}

@Composable
fun ReminderPriority.accentColor(): Color {
    val ext = LocalExtendedColors.current
    return when (this) {
        ReminderPriority.HIGH -> ext.priorityHigh
        ReminderPriority.MEDIUM -> ext.priorityMedium
        ReminderPriority.LOW -> ext.priorityLow
    }
}

@Composable
fun ReminderPriority.containerColor(): Color {
    val ext = LocalExtendedColors.current
    return when (this) {
        ReminderPriority.HIGH -> ext.priorityHighContainer
        ReminderPriority.MEDIUM -> ext.priorityMediumContainer
        ReminderPriority.LOW -> ext.priorityLowContainer
    }
}

@Composable
fun greetingStringRes(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
}

fun formatReminderDateTime(dueAtMillis: Long): String {
    val locale = Locale.forLanguageTag("pt-BR")
    val formatter = SimpleDateFormat("dd MMM, HH:mm", locale)
    return formatter.format(Date(dueAtMillis))
}

@Composable
fun DashboardHeroHeader(
    userDisplayName: String?,
    summary: ReminderSummaryCounts,
    modifier: Modifier = Modifier
) {
    val ext = LocalExtendedColors.current
    val firstName = userDisplayName
        ?.trim()
        ?.split(" ")
        ?.firstOrNull()
        ?.takeIf { it.isNotBlank() }

    val locale = Locale.forLanguageTag("pt-BR")
    val todayFormatted = SimpleDateFormat("EEEE, d 'de' MMMM", locale)
        .format(Date())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ext.heroGradientStart,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (firstName != null) {
                            "${stringResource(greetingStringRes())}, $firstName"
                        } else {
                            stringResource(greetingStringRes())
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard(
                    value = summary.dueTodayCount.toString(),
                    label = stringResource(R.string.summary_pending_today),
                    accentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    value = summary.overdueCount.toString(),
                    label = stringResource(R.string.summary_overdue),
                    accentColor = ext.priorityHigh,
                    containerColor = ext.priorityHighContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    value = summary.completedCount.toString(),
                    label = stringResource(R.string.summary_completed),
                    accentColor = ext.successAccent,
                    containerColor = ext.successContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    value: String,
    label: String,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: ReminderCategory?,
    onSelectCategory: (ReminderCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text(stringResource(R.string.cat_all)) },
                leadingIcon = if (selectedCategory == null) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("filter_cat_all")
            )
        }
        items(ReminderCategory.entries) { category ->
            val selected = selectedCategory == category
            val catColor = category.accentColor()
            FilterChip(
                selected = selected,
                onClick = { onSelectCategory(category) },
                label = { Text(stringResource(category.labelRes)) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon(),
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else catColor,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("filter_cat_${category.name.lowercase()}")
            )
        }
    }
}

@Composable
fun NotificationPermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("notification_permission_banner"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
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
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notif_banner_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = stringResource(R.string.notif_banner_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("enable_notifications_button")
            ) {
                Text(
                    text = stringResource(R.string.notif_banner_enable),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun ReminderEmptyState(
    tab: ReminderTab,
    hasActiveFilterOrSearch: Boolean,
    modifier: Modifier = Modifier
) {
    val (titleRes, subtitleRes) = if (hasActiveFilterOrSearch) {
        R.string.empty_search_title to R.string.empty_search_subtitle
    } else {
        when (tab) {
            ReminderTab.TODAY -> R.string.empty_today_title to R.string.empty_today_subtitle
            ReminderTab.UPCOMING -> R.string.empty_upcoming_title to R.string.empty_upcoming_subtitle
            ReminderTab.ALL -> R.string.empty_today_title to R.string.empty_today_subtitle
            ReminderTab.COMPLETED -> R.string.empty_completed_title to R.string.empty_completed_subtitle
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 28.dp)
            .testTag("empty_state_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_reminders),
            contentDescription = stringResource(R.string.empty_illustration_desc),
            modifier = Modifier
                .size(width = 210.dp, height = 158.dp)
                .clip(MaterialTheme.shapes.large),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
