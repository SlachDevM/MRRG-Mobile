package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.JobRepository
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.ui.components.snackbar.AppSnackbarManager
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.AFTER_PHOTO
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.BEFORE_PHOTO
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.JOB_COMPLETED
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.JOB_UPDATE_FAILED
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.NETWORK_ERROR
import com.slachdevm.mrrgmobile.ui.components.snackbar.SnackbarMessages.NOTES_SAVED
import kotlinx.coroutines.launch

data class JobDetailUiState(
    val job: Job? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false
)

class JobDetailViewModel(
    private val repository: JobRepository,
    private val jobId: Long
) : ViewModel() {

    var uiState by mutableStateOf(JobDetailUiState())
        private set

    init {
        loadJob()
    }

    private fun loadJob() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = repository.getJobDetails(jobId)
            uiState = if (result.isSuccess) {
                uiState.copy(job = result.getOrNull(), isLoading = false)
            } else {
                uiState.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to load job")
            }
        }
    }

    fun updateNotes(notes: String) {
        val currentJob = uiState.job ?: return
        val updatedJob = currentJob.copy(notes = notes)
        updateJob(updatedJob, successMessage = NOTES_SAVED)
    }

    fun addPhoto(url: String, isBefore: Boolean) {
        val currentJob = uiState.job ?: return
        val updatedBefore = if (isBefore) currentJob.beforePhotos + url else currentJob.beforePhotos
        val updatedAfter = if (!isBefore) currentJob.afterPhotos + url else currentJob.afterPhotos
        
        val updatedJob = currentJob.copy(
            beforePhotos = updatedBefore,
            afterPhotos = updatedAfter
        )
        updateJob(updatedJob, successMessage = if (isBefore) {
            BEFORE_PHOTO
        } else {
            AFTER_PHOTO
        })
    }

    private fun updateJob(
        updatedJob: Job,
        successMessage: String? = null
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isUpdating = true, error = null)

            try {
                val result = repository.updateJob(jobId, updatedJob)

                if (result.isSuccess) {
                    uiState = uiState.copy(
                        job = result.getOrNull(),
                        isUpdating = false,
                        updateSuccess = false
                    )

                    successMessage?.let {
                        AppSnackbarManager.showSuccess(it)
                    }
                } else {
                    uiState = uiState.copy(
                        isUpdating = false,
                        error = result.exceptionOrNull()?.message ?: "Unable to update job"
                    )

                    AppSnackbarManager.showError(JOB_UPDATE_FAILED)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isUpdating = false,
                    error = e.message ?: "Unexpected error"
                )

                AppSnackbarManager.showError(NETWORK_ERROR)
            }
        }
    }

    fun completeJob() {
        viewModelScope.launch {
            uiState = uiState.copy(isUpdating = true)
            val result = repository.completeJob(jobId)
            uiState = if (result.isSuccess) {
                AppSnackbarManager.showSuccess(JOB_COMPLETED)
                uiState.copy(isUpdating = false, updateSuccess = true)
            } else {
                AppSnackbarManager.showError(NETWORK_ERROR)
                uiState.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Failed to complete job")
            }
        }
    }

    fun clearUpdateSuccess() {
        uiState = uiState.copy(updateSuccess = false)
    }
}
