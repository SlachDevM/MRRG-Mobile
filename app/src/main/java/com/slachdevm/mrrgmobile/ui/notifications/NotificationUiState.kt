package com.slachdevm.mrrgmobile.ui.notifications

import com.slachdevm.mrrgmobile.domain.model.Notification

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)