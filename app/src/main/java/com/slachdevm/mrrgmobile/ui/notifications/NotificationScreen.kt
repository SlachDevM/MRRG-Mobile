package com.slachdevm.mrrgmobile.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.slachdevm.mrrgmobile.R
import com.slachdevm.mrrgmobile.domain.constants.JOB_ASSIGNED
import com.slachdevm.mrrgmobile.domain.constants.JOB_CONFIRMED
import com.slachdevm.mrrgmobile.domain.constants.JOB_READY_FOR_CONFIRMATION
import com.slachdevm.mrrgmobile.domain.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onBack: () -> Unit,
    onOpenJob: (Long) -> Unit
) {
    val state = viewModel.uiState
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isLoading
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = state.notifications.any { !it.read },
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = stringResource(R.string.action_mark_all_read)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    state.error != null -> {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    state.notifications.isEmpty() -> {
                        EmptyNotifications()
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = state.notifications,
                                key = { it.id }
                            ) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onClick = {
                                        if (!notification.read) {
                                            viewModel.markAsRead(notification.id)
                                        }

                                        notification.jobId?.let { jobId ->
                                            onOpenJob(jobId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!notification.read) {
                Badge(
                    modifier = Modifier.padding(top = 6.dp)
                ) {}

                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = notificationIcon(notification.type),
                contentDescription = null,
                tint = if (notification.read) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(
                    visible = !notification.read,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(stringResource(R.string.label_unread))
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Text(
                    text = notificationTitle(notification.type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Medium
                )

                notification.createdAt?.let {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatNotificationDate(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.no_notifications),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.all_caught_up),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun formatNotificationDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd MMM • HH:mm",
        Locale.getDefault()
    ).format(Date(timestamp))
}

private fun notificationIcon(type: String): ImageVector =
    when (type) {
        JOB_ASSIGNED -> Icons.Default.Assignment
        JOB_READY_FOR_CONFIRMATION -> Icons.Default.Schedule
        JOB_CONFIRMED -> Icons.Default.CheckCircle
        else -> Icons.Default.Notifications
    }

@Composable
private fun notificationTitle(type: String): String =
    when (type) {
        JOB_ASSIGNED -> stringResource(R.string.notif_new_job_assigned)
        JOB_READY_FOR_CONFIRMATION -> stringResource(R.string.notif_ready_for_confirmation)
        JOB_CONFIRMED -> stringResource(R.string.notif_job_confirmed)
        else -> stringResource(R.string.notif_generic_title)
    }
