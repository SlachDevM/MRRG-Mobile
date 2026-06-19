package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slachdevm.mrrgmobile.R
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.domain.model.UserRole
import com.slachdevm.mrrgmobile.ui.components.OfflineIndicator
import com.slachdevm.mrrgmobile.ui.components.toJobTypeLabel
import com.slachdevm.mrrgmobile.ui.components.toLabel
import com.slachdevm.mrrgmobile.ui.components.toPriorityLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardTopBar(
    userName: String?,
    isOfflineData: Boolean,
    viewMode: ViewMode,
    notificationUnreadCount: Long,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            DashboardHeader(
                userName = userName
            )
        },
        actions = {
            DashboardActions(
                isOfflineData = isOfflineData,
                viewMode = viewMode,
                notificationUnreadCount = notificationUnreadCount,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onToggleViewMode = onToggleViewMode,
                onSettingsClick = onSettingsClick,
                onLogout = onLogout
            )
        }
    )
}

@Composable
internal fun DashboardHeader(
    userName: String?
) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.greeting_morning)
        in 12..17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }

    Column {
        Text(
            text = "👋 $greeting",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            text = userName ?: stringResource(R.string.default_worker_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
internal fun DashboardActions(
    isOfflineData: Boolean,
    viewMode: ViewMode,
    notificationUnreadCount: Long,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val badgeScale by animateFloatAsState(
        targetValue = if (notificationUnreadCount > 0) 1f else 0f,
        animationSpec = tween(180),
        label = "NotificationBadgeScaleAnimation"
    )

    Column(horizontalAlignment = Alignment.End) {
        if (isOfflineData) {
            OfflineIndicator(modifier = Modifier.padding(end = 8.dp))
        }

        Row {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.action_profile)
                )
            }

            BadgedBox(
                badge = {
                    if (notificationUnreadCount > 0) {
                        Badge(
                            modifier = Modifier.graphicsLayer {
                                scaleX = badgeScale
                                scaleY = badgeScale
                            }
                        ) {
                            Text(notificationUnreadCount.toString())
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.action_notifications)
                    )
                }
            }

            IconButton(onClick = onToggleViewMode) {
                Icon(
                    imageVector = if (viewMode == ViewMode.DAY_3)
                        Icons.Default.CalendarMonth
                    else
                        Icons.Default.CalendarViewDay,
                    contentDescription = stringResource(R.string.action_toggle_view_mode)
                )
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.action_settings)
                )
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = stringResource(R.string.action_logout)
                )
            }
        }
    }
}

@Composable
internal fun DateRangeSelector(
    selectedDate: LocalDate,
    viewMode: ViewMode,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_previous)
            )
        }

        val endDate = selectedDate.plusDays(if (viewMode == ViewMode.DAY_3) 2 else 6)
        AnimatedContent(
            targetState = selectedDate to endDate,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                    animationSpec = tween(220),
                    initialOffsetX = { it / 4 }
                ) togetherWith fadeOut(animationSpec = tween(120)) + slideOutHorizontally(
                    animationSpec = tween(120),
                    targetOffsetX = { -it / 4 }
                ) using SizeTransform(clip = false)
            },
            label = "DateRangeAnimation"
        ) { (start, end) ->
            Text(
                text = "${start.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${
                    end.format(DateTimeFormatter.ofPattern("MMM dd"))
                }",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.action_next)
            )
        }
    }
}

@Composable
internal fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ErrorContent(error: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error ?: stringResource(R.string.error_something_went_wrong),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
internal fun EmptyJobsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(R.string.no_jobs_scheduled),
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = stringResource(R.string.no_work_planned_period),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
internal fun JobListContent(
    selectedDate: LocalDate,
    viewMode: ViewMode,
    jobsByDate: Map<LocalDate, List<Job>>,
    userName: String,
    userRole: UserRole?,
    onJobClick: (Long) -> Unit
) {
    val daysToShow = if (viewMode == ViewMode.DAY_3) 3 else 7
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
    ) {
        items(
            count = daysToShow,
            key = { index -> selectedDate.plusDays(index.toLong()) }
        ) { index ->
            val date = selectedDate.plusDays(index.toLong())
            val jobsForDate = jobsByDate[date] ?: emptyList()

            DaySection(
                modifier = Modifier.animateItem(),
                date = date,
                jobs = jobsForDate,
                currentUserName = userName,
                currentUserRole = userRole,
                onJobClick = onJobClick
            )
        }
    }
}

@Composable
internal fun DaySection(
    modifier: Modifier = Modifier,
    date: LocalDate,
    jobs: List<Job>,
    currentUserName: String,
    currentUserRole: UserRole?,
    onJobClick: (Long) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        if (jobs.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.no_jobs_scheduled),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = stringResource(R.string.no_work_planned_today),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            jobs.forEach { job ->
                val isAssigned =
                    job.assignedWorkers?.contains(currentUserName, ignoreCase = true) == true
                val canEdit =
                    isAssigned || currentUserRole == UserRole.MANAGER || currentUserRole == UserRole.ADMIN

                JobItem(
                    job = job,
                    canEdit = canEdit,
                    onClick = { if (canEdit) job.id?.let { onJobClick(it) } }
                )
            }
        }
    }
}

@Composable
internal fun JobItem(
    job: Job,
    canEdit: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && canEdit) 0.98f else 1f,
        animationSpec = tween(120),
        label = "JobCardPressAnimation"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        onClick = onClick,
        enabled = canEdit,
        interactionSource = interactionSource,
        colors = if (canEdit) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            )
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.jobTypes.toJobTypeLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (canEdit) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )

                if (!canEdit) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.status_not_assigned),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = job.status.toLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (job.priorityLevel >= 3) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.clientName,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (canEdit) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.weight(1f)
                )

                if (canEdit && job.priorityLevel >= 3) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Badge(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(
                            text = job.priorityLevel.toPriorityLabel(),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = job.clientAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (canEdit) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }

            if (!job.assignedWorkers.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = job.assignedWorkers,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (canEdit) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }
        }
    }
}
