package com.slachdevm.mrrgmobile.ui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    var uiState by mutableStateOf(NotificationUiState())
        private set

    fun refresh() {
        viewModelScope.launch {
            refreshState(showLoading = true)
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            refreshUnreadCount()
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(
                notifications = uiState.notifications.map {
                    if (it.id == id) it.copy(read = true) else it
                },
                unreadCount = (uiState.unreadCount - 1).coerceAtLeast(0)
            )

            repository.markAsRead(id)
            refreshState(showLoading = false)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            uiState = uiState.copy(
                notifications = uiState.notifications.map { it.copy(read = true) },
                unreadCount = 0
            )

            repository.markAllAsRead()
            refreshState(showLoading = false)
        }
    }

    private suspend fun refreshState(showLoading: Boolean) {
        refreshNotifications(showLoading)
        refreshUnreadCount()
    }

    private suspend fun refreshNotifications(showLoading: Boolean) {
        if (showLoading) {
            uiState = uiState.copy(isLoading = true)
        }

        val result = repository.getNotifications()

        uiState = if (result.isSuccess) {
            uiState.copy(
                notifications = result.getOrDefault(emptyList()),
                isLoading = false,
                error = null
            )
        } else {
            uiState.copy(
                isLoading = false,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    private suspend fun refreshUnreadCount() {
        repository.getUnreadCount().onSuccess { unreadCount ->
            uiState = uiState.copy(unreadCount = unreadCount)
        }
    }
}
