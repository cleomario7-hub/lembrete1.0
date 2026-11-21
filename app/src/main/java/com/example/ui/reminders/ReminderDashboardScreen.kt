package com.example.ui.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.Reminder
import com.example.data.ReminderCategory
import com.example.data.ReminderRepository
import com.example.data.ReminderTab
import com.example.data.UiState
import com.example.notifications.ReminderNotificationScheduler

private fun ReminderTab.icon(selected: Boolean): ImageVector = when (this) {
    ReminderTab.TODAY -> if (selected) Icons.Filled.Today else Icons.Outlined.Today
    ReminderTab.UPCOMING -> if (selected) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth
    ReminderTab.ALL -> if (selected) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.AutoMirrored.Outlined.FormatListBulleted
    ReminderTab.COMPLETED -> if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
}

@Composable
fun ReminderDashboardScreen(
    currentUserId: String,
    userDisplayName: String?
) {
    val context = LocalContext.current

    val viewModel: ReminderViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY ausente em CreationExtras"
                }
                ReminderViewModel(
                    repository = ReminderRepository.get(app),
                    scheduler = ReminderNotificationScheduler(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val remindersState by viewModel.remindersState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isEditorOpen by viewModel.isEditorOpen.collectAsStateWithLifecycle()
    val editingReminder by viewModel.editingReminder.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var hasNotifPermission by remember {
        mutableStateOf(ReminderNotificationScheduler.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessages.collect { msg ->
            val resolved = when (msg) {
                is SnackbarMessage.Resource -> context.getString(msg.resId)
                is SnackbarMessage.Dynamic -> msg.text
            }
            snackbarHostState.showSnackbar(resolved)
        }
    }

    val allReminders = (remindersState as? UiState.Success)?.data.orEmpty()
    val summary = remember(allReminders) {
        ReminderViewModel.computeSummary(allReminders)
    }
    val visibleReminders = remember(allReminders, selectedTab, selectedCategory, searchQuery) {
        ReminderViewModel.filterReminders(
            reminders = allReminders,
            tab = selectedTab,
            category = selectedCategory,
            query = searchQuery
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 840.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openNewReminderSheet() },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.fab_new_reminder)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_reminder_fab")
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        ReminderTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(selected),
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (isExpandedScreen) {
                // Canonical NavigationRail + Supporting Two-Pane Layout for Tablets & Foldables
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ReminderTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(selected),
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        DashboardHeroHeader(
                            userDisplayName = userDisplayName,
                            summary = summary,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
                            NotificationPermissionBanner(
                                onRequestPermission = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            )
                        }
                    }

                    ReminderListPane(
                        remindersState = remindersState,
                        visibleReminders = visibleReminders,
                        selectedTab = selectedTab,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        showHeroHeaderInList = false,
                        userDisplayName = userDisplayName,
                        summary = summary,
                        showNotifBanner = false,
                        onRequestNotifPermission = {},
                        onSearchChange = viewModel::updateSearchQuery,
                        onSelectCategory = viewModel::selectCategory,
                        onToggleComplete = viewModel::toggleComplete,
                        onEditReminder = viewModel::openEditReminderSheet,
                        onSnoozeOneHour = viewModel::snoozeOneHour,
                        onSnoozeTomorrow = viewModel::snoozeTomorrowMorning,
                        onDeleteReminder = viewModel::deleteReminder,
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    )
                }
            } else {
                ReminderListPane(
                    remindersState = remindersState,
                    visibleReminders = visibleReminders,
                    selectedTab = selectedTab,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    showHeroHeaderInList = true,
                    userDisplayName = userDisplayName,
                    summary = summary,
                    showNotifBanner = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission,
                    onRequestNotifPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onSearchChange = viewModel::updateSearchQuery,
                    onSelectCategory = viewModel::selectCategory,
                    onToggleComplete = viewModel::toggleComplete,
                    onEditReminder = viewModel::openEditReminderSheet,
                    onSnoozeOneHour = viewModel::snoozeOneHour,
                    onSnoozeTomorrow = viewModel::snoozeTomorrowMorning,
                    onDeleteReminder = viewModel::deleteReminder,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }

    if (isEditorOpen) {
        ReminderEditorSheet(
            existingReminder = editingReminder,
            isSaving = isSaving,
            onDismiss = viewModel::dismissEditorSheet,
            onSave = viewModel::saveReminder,
            onDelete = viewModel::deleteReminder
        )
    }
}

@Composable
private fun ReminderListPane(
    remindersState: UiState<List<Reminder>>,
    visibleReminders: List<Reminder>,
    selectedTab: ReminderTab,
    selectedCategory: ReminderCategory?,
    searchQuery: String,
    showHeroHeaderInList: Boolean,
    userDisplayName: String?,
    summary: ReminderSummaryCounts,
    showNotifBanner: Boolean,
    onRequestNotifPermission: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (ReminderCategory?) -> Unit,
    onToggleComplete: (Reminder) -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onSnoozeOneHour: (Reminder) -> Unit,
    onSnoozeTomorrow: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("reminders_lazy_column"),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showHeroHeaderInList) {
                item(key = "hero_header") {
                    DashboardHeroHeader(
                        userDisplayName = userDisplayName,
                        summary = summary
                    )
                }
            }

            if (showNotifBanner) {
                item(key = "notif_banner") {
                    NotificationPermissionBanner(
                        onRequestPermission = onRequestNotifPermission
                    )
                }
            }

            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.clear_search)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .testTag("search_reminders_input")
                )
            }

            item(key = "category_chips") {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = onSelectCategory
                )
            }

            when (remindersState) {
                is UiState.Loading -> {
                    item(key = "loading_indicator") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item(key = "error_banner") {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("firestore_error_banner")
                        ) {
                            Text(
                                text = remindersState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    if (visibleReminders.isEmpty()) {
                        item(key = "empty_state") {
                            ReminderEmptyState(
                                tab = selectedTab,
                                hasActiveFilterOrSearch = selectedCategory != null || searchQuery.isNotBlank()
                            )
                        }
                    } else {
                        items(
                            items = visibleReminders,
                            key = { it.id.ifBlank { "${it.title}_${it.dueAtMillis}" } }
                        ) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onToggleComplete = { onToggleComplete(reminder) },
                                onCardClick = { onEditReminder(reminder) },
                                onSnoozeOneHour = { onSnoozeOneHour(reminder) },
                                onSnoozeTomorrow = { onSnoozeTomorrow(reminder) },
                                onDelete = { onDeleteReminder(reminder) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
