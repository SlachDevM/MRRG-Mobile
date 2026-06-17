package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import com.slachdevm.mrrgmobile.data.repository.JobRepository
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.domain.model.UserRole
import com.slachdevm.mrrgmobile.ui.components.snackbar.AppSnackbarManager
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.JOBS_UPDATED
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.NETWORK_ERROR
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class ViewMode {
    DAY_3, WEEK
}

data class JobListUiState(
    val jobsByDate: Map<LocalDate, List<Job>> = emptyMap(),
    val userName: String? = null,
    val userRole: UserRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val viewMode: ViewMode = ViewMode.DAY_3,
    val selectedDate: LocalDate = LocalDate.now()
)

class JobListViewModel(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(JobListUiState())
        private set

    init {
        uiState = uiState.copy(
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
            
            val startMillis = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val result = jobRepository.getScheduledJobs(startMillis, endMillis)
            
            uiState = if (result.isSuccess) {
                val jobs = result.getOrDefault(emptyList())
                val grouped = jobs.filter { it.jobDate != null }.groupBy {
                    Instant.ofEpochMilli(it.jobDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                onSuccess?.invoke()
                uiState.copy(jobsByDate = grouped, isLoading = false)
            } else {
                AppSnackbarManager.showError(NETWORK_ERROR)
                uiState.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to load jobs")
            }
        }
    }

    fun refreshJobs() {
        viewModelScope.launch {

            loadJobs {
                AppSnackbarManager.showInfo(
                    JOBS_UPDATED
                )
            }
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
