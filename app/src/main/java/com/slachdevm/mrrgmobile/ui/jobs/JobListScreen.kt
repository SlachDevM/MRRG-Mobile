package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.domain.model.UserRole
import com.slachdevm.mrrgmobile.ui.components.toJobTypeLabel
import com.slachdevm.mrrgmobile.ui.components.toLabel
import com.slachdevm.mrrgmobile.ui.components.toPriorityLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    viewModel: JobListViewModel,
    onJobClick: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${state.userName ?: "Unknown"} (${state.userRole?.toLabel() ?: "No Role"})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.DAY_3) Icons.Default.CalendarMonth else Icons.Default.CalendarViewDay,
                            contentDescription = "Toggle View Mode"
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
                
                val endDate = state.selectedDate.plusDays(if (state.viewMode == ViewMode.DAY_3) 2 else 6)
                Text(
                    text = "${state.selectedDate.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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
                if (state.isLoading && state.jobsByDate.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null && state.jobsByDate.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    val daysToShow = if (state.viewMode == ViewMode.DAY_3) 3 else 7

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items((0 until daysToShow).toList()) { index ->
                            val date = state.selectedDate.plusDays(index.toLong())
                            val jobsForDate = state.jobsByDate[date] ?: emptyList()

                            DaySection(
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

@Composable
fun DaySection(
    date: LocalDate,
    jobs: List<Job>,
    currentUserName: String,
    currentUserRole: UserRole?,
    onJobClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (jobs.isEmpty()) {
            Text(
                text = "No jobs scheduled",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            jobs.forEach { job ->
                val isAssigned = job.assignedWorkers?.contains(currentUserName, ignoreCase = true) == true
                val canEdit = isAssigned || currentUserRole == UserRole.MANAGER || currentUserRole == UserRole.ADMIN
                
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        enabled = canEdit,
        colors = if (canEdit) CardDefaults.cardColors() else CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
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
                    color = if (canEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                        color = if (job.priorityLevel >= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.clientName,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (canEdit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (canEdit && job.priorityLevel >= 3) {
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
            Text(
                text = job.clientAddress, 
                style = MaterialTheme.typography.bodyMedium,
                color = if (canEdit) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            if (!job.assignedWorkers.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Workers: ${job.assignedWorkers}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canEdit) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
