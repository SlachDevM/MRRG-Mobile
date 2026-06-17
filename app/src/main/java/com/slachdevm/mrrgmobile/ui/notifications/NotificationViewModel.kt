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

    init {
        refresh()
    }

    fun refresh() {
        loadNotifications()
        loadUnreadCount()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

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
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount().onSuccess {
                uiState = uiState.copy(unreadCount = it)
            }
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

            loadUnreadCount()
            loadNotifications()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            uiState = uiState.copy(
                notifications = uiState.notifications.map {
                    it.copy(read = true)
                },
                unreadCount = 0
            )

            repository.markAllAsRead()

            loadUnreadCount()
            loadNotifications()
        }
    }
}