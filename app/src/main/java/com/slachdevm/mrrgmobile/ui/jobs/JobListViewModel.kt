package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import com.slachdevm.mrrgmobile.data.repository.JobRepository
import com.slachdevm.mrrgmobile.data.repository.NotificationRepository
import com.slachdevm.mrrgmobile.data.sync.SyncRepository
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.domain.model.UserRole
import com.slachdevm.mrrgmobile.ui.components.snackbar.AppSnackbarManager
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.JOBS_UPDATED
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.NETWORK_ERROR
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ViewMode {
    DAY_3, WEEK
}

data class JobListUiState(
    val jobsByDate: Map<LocalDate, List<Job>> = emptyMap(),
    val userId: Long = -1L,
    val userName: String? = null,
    val userRole: UserRole? = null,
    val isLoading: Boolean = false,
    val isOfflineData: Boolean = false,
    val error: String? = null,
    val viewMode: ViewMode = ViewMode.DAY_3,
    val selectedDate: LocalDate = LocalDate.now()
)

class JobListViewModel(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var uiState by mutableStateOf(JobListUiState())
        private set

    init {
        uiState = uiState.copy(
            userId = authRepository.getUserId(),
            userName = authRepository.getUserName(),
            userRole = authRepository.getUserRole()
        )
        loadJobs()
    }

    fun loadJobs(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            val start = uiState.selectedDate
            val end = if (uiState.viewMode == ViewMode.DAY_3) start.plusDays(2) else start.plusDays(6)

            syncRepository.synchronize()
                .onFailure {
                    // Pending items stay queued.
                }
            val result = jobRepository.getScheduledJobs(start, end)
            
            uiState = if (result.isSuccess) {
                val dataSourceResult = result.getOrNull()
                val jobs = dataSourceResult?.data.orEmpty()
                
                // Grouping by LocalDate
                val grouped = jobs.mapNotNull { job ->
                    job.jobDate?.let { date ->
                        date to job
                    }
                }.groupBy({ it.first }, { it.second })

                onSuccess?.invoke()
                uiState.copy(
                    jobsByDate = grouped,
                    isLoading = false,
                    isOfflineData = dataSourceResult?.isOfflineData == true
                )
            } else {
                AppSnackbarManager.showError(NETWORK_ERROR)
                uiState.copy(
                    isLoading = false,
                    isOfflineData = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load jobs"
                )
            }
        }
    }

    fun refreshJobs() {
        loadJobs {
            AppSnackbarManager.showInfo(
                JOBS_UPDATED
            )
        }
    }

    fun toggleViewMode() {
        uiState = uiState.copy(
            viewMode = if (uiState.viewMode == ViewMode.DAY_3) ViewMode.WEEK else ViewMode.DAY_3
        )
        loadJobs()
    }

    fun nextDateRange() {
        val daysToMove = if (uiState.viewMode == ViewMode.DAY_3) 3L else 7L
        uiState = uiState.copy(selectedDate = uiState.selectedDate.plusDays(daysToMove))
        loadJobs()
    }

    fun previousDateRange() {
        val daysToMove = if (uiState.viewMode == ViewMode.DAY_3) 3L else 7L
        uiState = uiState.copy(selectedDate = uiState.selectedDate.minusDays(daysToMove))
        loadJobs()
    }

    fun logout() {
        authRepository.logout()
    }
}
