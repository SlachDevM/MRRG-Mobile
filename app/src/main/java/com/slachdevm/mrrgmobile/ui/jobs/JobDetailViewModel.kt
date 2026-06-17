package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.JobRepository
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.ui.components.snackbar.AppSnackbarManager
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
                AppSnackbarManager.showSuccess("Job loaded successfully")
                uiState.copy(job = result.getOrNull(), isLoading = false)
            } else {
                AppSnackbarManager.showError("Failed to load job")
                uiState.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to load job")
            }
        }
    }

    fun updateNotes(notes: String) {
        val currentJob = uiState.job ?: return
        val updatedJob = currentJob.copy(notes = notes)
        updateJob(updatedJob, successMessage = "Notes saved")
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
            "Before photo uploaded"
        } else {
            "After photo uploaded"
        })
    }

    private fun updateJob(
        updatedJob: Job,
        successMessage: String? = null
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isUpdating = true, error = null)

            try {
                val result = repository.updateJob(updatedJob.id, updatedJob)

                if (result.isSuccess) {
                    uiState = uiState.copy(
                        job = result.getOrNull(),
                        isUpdating = false,
                        updateSuccess = true
                    )

                    successMessage?.let {
                        AppSnackbarManager.showSuccess(it)
                    }
                } else {
                    uiState = uiState.copy(
                        isUpdating = false,
                        error = result.exceptionOrNull()?.message ?: "Unable to update job"
                    )

                    AppSnackbarManager.showError("Unable to update job")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isUpdating = false,
                    error = e.message ?: "Unexpected error"
                )

                AppSnackbarManager.showError("Unexpected error")
            }
        }
    }

    fun completeJob() {
        viewModelScope.launch {
            uiState = uiState.copy(isUpdating = true)
            val result = repository.completeJob(jobId)
            uiState = if (result.isSuccess) {
                AppSnackbarManager.showSuccess("Job completed")
                uiState.copy(isUpdating = false, updateSuccess = true)
            } else {
                AppSnackbarManager.showError("Failed to complete job")
                uiState.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Failed to complete job")
            }
        }
    }

    fun clearUpdateSuccess() {
        uiState = uiState.copy(updateSuccess = false)
    }
}
