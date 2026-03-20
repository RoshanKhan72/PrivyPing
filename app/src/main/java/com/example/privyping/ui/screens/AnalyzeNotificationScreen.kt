package com.example.privyping.ui.screens

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.privyping.ui.analysis.RiskLevel
import com.example.privyping.ui.model.NotificationItem
import com.example.privyping.ui.viewmodel.NotificationViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.FilterList
import kotlinx.coroutines.launch


/* ---------------- FILTER DROPDOWN ---------------- */

@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.height(40.dp)
            ) {
                Text(selectedOption, fontSize = 12.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeNotificationScreen(
    onOpenAnalysis: (String) -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {

    val notifications by viewModel.notifications.collectAsState()
    // Filter out items that are soft-deleted
    val visibleNotifications = notifications.filter { !it.isDeleted }

    val context = LocalContext.current

    var showClearAllDialog by remember { mutableStateOf(false) }
    var showNoNewDialog by remember { mutableStateOf(false) }
    var lastRefreshCount by remember { mutableStateOf(notifications.size) }
    var selectionMode by remember { mutableStateOf(false) }

    var selectedAppFilter by remember { mutableStateOf("All") }
    var selectedTimeFilter by remember { mutableStateOf("24 hours") }
    var selectedRiskFilter by remember { mutableStateOf("All") }

    var customTimeStart by remember { mutableStateOf("") }
    var customTimeEnd by remember { mutableStateOf("") }
    var customAppQuery by remember { mutableStateOf("") }

    val selectedIds = remember { mutableStateSetOf<String>() }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()

    // Confirmation input state
    var confirmationInput by remember { mutableStateOf("") }

    /* ---------- FILTERED LIST ---------- */
    // Apply filters to visible (non-deleted) notifications
    val filteredNotifications = applyFilters(
        notifications = visibleNotifications,
        appFilter = selectedAppFilter,
        timeFilter = selectedTimeFilter,
        riskFilter = selectedRiskFilter,
        customAppQuery = customAppQuery,
        customTimeStart = customTimeStart,
        customTimeEnd = customTimeEnd
    ).sortedByDescending { it.timestamp } // ✅ Ensure newest is always top

    // ✅ AUTO-SCROLL TO TOP WHEN NEW NOTIFICATION ARRIVES
    LaunchedEffect(filteredNotifications.size) {
        if (filteredNotifications.isNotEmpty() && !selectionMode) {
            listState.animateScrollToItem(0)
        }
    }

    val appFilters = listOf("All", "WhatsApp", "Gmail", "Custom")
    val timeFilters = listOf(
        "All time",
        "Last 1 hour",
        "24 hours",
        "7 days",
        "1 month",
        "6 months",
        "Custom"
    )
    val riskFilters = listOf("All", "Low", "Medium", "High")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(

                title = {
                    if (selectionMode) {
                        Text("${selectedIds.size} selected")
                    } else {
                        Text("Analyze Notifications")
                    }
                },
                navigationIcon = {
                    if (selectionMode) {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedIds.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                },
                actions = {
                    if (selectionMode) {
                        TextButton(onClick = {
                            selectedIds.clear()
                            filteredNotifications.forEach { selectedIds.add(it.id) }
                        }) {
                            Text("Select all")
                        }

                        IconButton(onClick = { 
                            confirmationInput = ""
                            showDeleteSelectedDialog = true 
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    } else {
                        // ✅ ENHANCED REFRESH BUTTON
                        IconButton(onClick = {
                            viewModel.refreshNotifications { addedCount ->
                                scope.launch {
                                    if (addedCount > 0) {
                                        snackbarHostState.showSnackbar("Refreshed: $addedCount new notifications synced")
                                    } else {
                                        snackbarHostState.showSnackbar("Synced: Everything up to date")
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }

                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }

                        IconButton(
                            onClick = { 
                                confirmationInput = ""
                                showClearAllDialog = true 
                            },
                            enabled = visibleNotifications.isNotEmpty()
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->



        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            if (filteredNotifications.isEmpty()) {
                Text("No notifications captured yet")
            } else {
                LazyColumn(state = listState) {
                    items(filteredNotifications, key = { it.id }) { item ->
                        
                        val deleteAction = {
                            // 1️⃣ Soft delete (hide immediately)
                            viewModel.softDelete(item)

                            // 2️⃣ Show Undo
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Notification deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short // ~4 sec
                                )

                                // 3️⃣ Undo or finalize
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreNotification(item)
                                } else {
                                    viewModel.hardDeleteNotification(item)
                                }
                            }
                        }

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    deleteAction()
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(500),
                                fadeOutSpec = tween(500),
                                placementSpec = tween(500)
                            ),
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 6.dp)
                                        .background(color, shape = MaterialTheme.shapes.medium)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            NotificationCard(
                                item = item,
                                selectionMode = selectionMode,
                                isSelected = selectedIds.contains(item.id),
                                onLongPress = {
                                    selectionMode = true
                                    selectedIds.add(item.id)
                                },
                                onSelectToggle = {
                                    if (selectedIds.contains(item.id)) {
                                        selectedIds.remove(item.id)
                                    } else {
                                        selectedIds.add(item.id)
                                    }
                                    if (selectedIds.isEmpty()) selectionMode = false
                                },
                                onReAnalyze = {
                                    viewModel.reAnalyze(item)
                                },
                                onOpenDetails = {
                                    scope.launch {
                                        if (showFilterSheet) {
                                            sheetState.hide()
                                            showFilterSheet = false
                                        }
                                        onOpenAnalysis(item.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showFilterSheet = false },
            scrimColor = Color.Transparent

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 360.dp) // ✅ KEY LINE
                    .padding(16.dp)
            ){

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        selectedAppFilter = "All"
                        selectedTimeFilter = "All time"
                        selectedRiskFilter = "All"
                        customAppQuery = ""
                        customTimeStart = ""
                        customTimeEnd = ""
                    }) {
                        Text("Clear all")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterDropdown("App", appFilters, selectedAppFilter) {
                        selectedAppFilter = it
                    }
                    FilterDropdown("Period", timeFilters, selectedTimeFilter) {
                        selectedTimeFilter = it
                    }
                    FilterDropdown("Risk", riskFilters, selectedRiskFilter) {
                        selectedRiskFilter = it
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (selectedTimeFilter == "Custom") {
                    OutlinedTextField(
                        value = customTimeStart,
                        onValueChange = { customTimeStart = it },
                        label = { Text("Start timestamp (ms)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customTimeEnd,
                        onValueChange = { customTimeEnd = it },
                        label = { Text("End timestamp (ms)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedAppFilter == "Custom") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customAppQuery,
                        onValueChange = { customAppQuery = it },
                        label = { Text("Enter app name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showFilterSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C5791),
                        contentColor = Color.White
                    )
                ) {
                    Text("Apply")
                }
            }
        }
    }


    /* ---------------- DIALOGS ---------------- */

    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            title = { Text("Confirm delete") },
            text = { 
                Column {
                    Text("Are you sure you want to delete ${selectedIds.size} selected messages?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Type 'Confirm' to enable delete button:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = { confirmationInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = confirmationInput == "Confirm",
                    onClick = {
                        // Identify ALL items to process
                        val itemsToProcess = visibleNotifications.filter { it.id in selectedIds }
                        
                        // 1️⃣ Soft delete ALL selected (hide immediately)
                        viewModel.softDeleteBatch(itemsToProcess)
                        
                        selectedIds.clear()
                        selectionMode = false
                        showDeleteSelectedDialog = false
                        
                        // 2️⃣ Show Undo Snackbar
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${itemsToProcess.size} notifications deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            
                            // 3️⃣ Undo or finalize for the whole batch
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreBatch(itemsToProcess)
                            } else {
                                viewModel.hardDeleteBatch(itemsToProcess)
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNoNewDialog) {
        AlertDialog(
            onDismissRequest = { showNoNewDialog = false },
            title = { Text("No new notifications") },
            text = { Text("You're all caught up.") },
            confirmButton = {
                TextButton(onClick = { showNoNewDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear all notifications?") },
            text = {
                Column {
                    Text("This will move all notifications to trash. You can undo this action.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Type 'Confirm' to enable delete button:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = { confirmationInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = confirmationInput == "Confirm",
                    onClick = {
                        showClearAllDialog = false

                        val itemsToDelete = visibleNotifications.toList()

                        // 1️⃣ Soft delete all (hide immediately)
                        viewModel.softDeleteBatch(itemsToDelete)

                        // 2️⃣ Show Undo Snackbar
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "All notifications cleared",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )

                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreBatch(itemsToDelete)
                            } else {
                                viewModel.hardDeleteBatch(itemsToDelete)
                            }
                        }
                    }
                ) {
                    Text("Delete all", color = if (confirmationInput == "Confirm") Color.Red else Color.Gray)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


/* ---------------- CARD ---------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCard(
    item: NotificationItem,
    selectionMode: Boolean,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onSelectToggle: () -> Unit,
    onReAnalyze: () -> Unit,
    onOpenDetails: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var messageExpanded by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val now = System.currentTimeMillis()
    val diff = now - item.timestamp
    val timeAgo = if (diff < 60000) {
        "just now"
    } else {
        DateUtils.getRelativeTimeSpanString(
            item.timestamp,
            now,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (selectionMode) onSelectToggle() else onOpenDetails()
                },
                onLongClick = onLongPress
            )
    ) {
        Column(Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(isSelected, onCheckedChange = { onSelectToggle() })
                        Spacer(Modifier.width(8.dp))
                        Text(item.appName, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(item.appName, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = timeAgo,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Text(item.senderName, fontSize = 14.sp)

            Text(
                item.message,
                maxLines = if (messageExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (item.message.length > 120) {
                Text(
                    text = if (messageExpanded) "See less" else "See more…",
                    color = Color(0xFF1E88E5),
                    fontSize = 12.sp,
                    modifier = Modifier.noRippleClickable {
                        messageExpanded = !messageExpanded
                    }
                )
            }

            Text(
                text = "Risk: ${item.riskLevel} (${item.confidence}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when (item.riskLevel) {
                    RiskLevel.HIGH -> Color.Red
                    RiskLevel.MEDIUM -> Color(0xFFFFA000)
                    RiskLevel.LOW -> Color(0xFF2E7D32)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide analysis" else "Check analysis",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.noRippleClickable {
                        expanded = !expanded
                    }
                )

                Row {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(item.message))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                }
            }

            if (expanded && item.summary != null) {
                Text(item.summary, fontSize = 12.sp)
                Text(
                    text = "Re-analyze",
                    color = Color(0xFF1E88E5),
                    fontSize = 12.sp,
                    modifier = Modifier.noRippleClickable {
                        onReAnalyze()
                    }
                )
            }
        }
    }
}
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )


/* ---------------- FILTER LOGIC ---------------- */

private fun applyFilters(
    notifications: List<NotificationItem>,
    appFilter: String,
    timeFilter: String,
    riskFilter: String,
    customAppQuery: String,
    customTimeStart: String,
    customTimeEnd: String
): List<NotificationItem> {


    val now = System.currentTimeMillis()

    val timeFiltered = notifications.filter { item ->
        when (timeFilter) {
            "All time" -> true
            "Last 1 hour" -> item.timestamp >= now - 3_600_000
            "Last 24 hours" -> item.timestamp >= now - 86_400_000
            "Last 7 days" -> item.timestamp >= now - 604_800_000
            "Last 1 month" -> item.timestamp >= now - 2_592_000_000
            "Last 6 months" -> item.timestamp >= now - 15_552_000_000
            "Custom" -> {
                val start = customTimeStart.toLongOrNull() ?: 0L
                val end = customTimeEnd.toLongOrNull() ?: Long.MAX_VALUE
                item.timestamp in start..end
            }
            else -> true
        }
    }

    return timeFiltered
        .filter { item ->
            when (appFilter) {
                "WhatsApp" -> item.packageName.contains("whatsapp", true)
                "Gmail" -> item.packageName.contains("gmail", true)
                "Custom" ->
                    customAppQuery.isNotBlank() &&
                            (item.packageName.contains(customAppQuery, true)
                                    || item.appName.contains(customAppQuery, true))
                else -> true
            }
        }
        .filter { item ->
            when (riskFilter) {
                "Low" -> item.riskLevel == RiskLevel.LOW
                "Medium" -> item.riskLevel == RiskLevel.MEDIUM
                "High" -> item.riskLevel == RiskLevel.HIGH
                else -> true
            }
        }

}
