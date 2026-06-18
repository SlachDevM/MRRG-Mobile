package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.domain.model.UserRole
import com.slachdevm.mrrgmobile.ui.components.toJobTypeLabel
import com.slachdevm.mrrgmobile.ui.components.toLabel
import com.slachdevm.mrrgmobile.ui.components.toPriorityLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slachdevm.mrrgmobile.ui.components.OfflineIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    viewModel: JobListViewModel,
    notificationUnreadCount: Long,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onJobClick: (Long) -> Unit,
    onLogout: () -> Unit,
) {
    val state = viewModel.uiState
    val hour = java.time.LocalTime.now().hour

    val badgeScale by animateFloatAsState(
        targetValue = if (notificationUnreadCount > 0) 1f else 0f,
        animationSpec = tween(180),
        label = "NotificationBadgeScaleAnimation"
    )

    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "👋 $greeting",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Text(
                            text = state.userName ?: "Worker",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = state.userRole?.displayName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                },
                actions = {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        if (state.isOfflineData) {
                            OfflineIndicator(
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        Row {
                            IconButton(
                                onClick = onProfileClick
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
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
                                IconButton(
                                    onClick = onNotificationsClick
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications"
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.toggleViewMode() }) {
                                Icon(
                                    imageVector = if (state.viewMode == ViewMode.DAY_3)
                                        Icons.Default.CalendarMonth
                                    else
                                        Icons.Default.CalendarViewDay,
                                    contentDescription = "Toggle View Mode"
                                )
                            }

                            IconButton(onClick = onSettingsClick) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }

                            IconButton(onClick = {
                                viewModel.logout()
                                onLogout()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Date Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousDateRange() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                }

                val endDate =
                    state.selectedDate.plusDays(if (state.viewMode == ViewMode.DAY_3) 2 else 6)
                AnimatedContent(
                    targetState = state.selectedDate to endDate,
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
                ) { (startDate, endDate) ->
                    Text(
                        text = "${startDate.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${
                            endDate.format(DateTimeFormatter.ofPattern("MMM dd"))
                        }",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { viewModel.nextDateRange() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }

            val swipeRefreshState = rememberSwipeRefreshState(
                isRefreshing = state.isLoading
            )

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refreshJobs() },
                modifier = Modifier.fillMaxSize()
            ) {
                val hasJobs = state.jobsByDate.values.any { it.isNotEmpty() }
                val daysToShow = if (state.viewMode == ViewMode.DAY_3) 3 else 7

                val dashboardContentState = when {
                    state.isLoading && state.jobsByDate.isEmpty() -> "loading"
                    state.error != null && state.jobsByDate.isEmpty() -> "error"
                    !state.isLoading && !hasJobs -> "empty"
                    else -> "content"
                }

                Crossfade(
                    targetState = dashboardContentState,
                    label = "DashboardContentAnimation"
                ) { contentState ->
                    when (contentState) {
                        "loading" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        "error" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.error ?: "Something went wrong",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        "empty" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.6f
                                        )
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
                                                text = "No jobs scheduled",
                                                style = MaterialTheme.typography.titleSmall
                                            )

                                            Text(
                                                text = "No work planned for this period.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                            ) {
                                items(
                                    count = daysToShow,
                                    key = { index ->
                                        state.selectedDate.plusDays(index.toLong())
                                    }
                                ) { index ->
                                    val date = state.selectedDate.plusDays(index.toLong())
                                    val jobsForDate = state.jobsByDate[date] ?: emptyList()

                                    DaySection(
                                        modifier = Modifier.animateItem(),
                                        date = date,
                                        jobs = jobsForDate,
                                        currentUserName = state.userName ?: "",
                                        currentUserRole = state.userRole,
                                        onJobClick = onJobClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaySection(
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
                            text = "No jobs scheduled",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "No work planned today. Enjoy your day!",
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
fun JobItem(
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
                        contentDescription = "Not Assigned",
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
                        text = job.assignedWorkers ?: "",
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