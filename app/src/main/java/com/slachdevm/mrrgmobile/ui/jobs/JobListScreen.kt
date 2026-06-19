package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

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

    Scaffold(
        topBar = {
            DashboardTopBar(
                userName = state.userName,
                isOfflineData = state.isOfflineData,
                viewMode = state.viewMode,
                notificationUnreadCount = notificationUnreadCount,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onToggleViewMode = viewModel::toggleViewMode,
                onSettingsClick = onSettingsClick,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            DateRangeSelector(
                selectedDate = state.selectedDate,
                viewMode = state.viewMode,
                onPreviousClick = viewModel::previousDateRange,
                onNextClick = viewModel::nextDateRange
            )

            val swipeRefreshState = rememberSwipeRefreshState(
                isRefreshing = state.isLoading
            )

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refreshJobs() },
                modifier = Modifier.fillMaxSize()
            ) {
                val hasJobs = state.jobsByDate.values.any { it.isNotEmpty() }
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
                        "loading" -> LoadingContent()
                        "error" -> ErrorContent(state.error)
                        "empty" -> EmptyJobsContent()
                        else -> JobListContent(
                            selectedDate = state.selectedDate,
                            viewMode = state.viewMode,
                            jobsByDate = state.jobsByDate,
                            userName = state.userName ?: "",
                            userRole = state.userRole,
                            onJobClick = onJobClick
                        )
                    }
                }
            }
        }
    }
}
