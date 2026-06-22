package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.api.NotificationApi
import com.slachdevm.mrrgmobile.data.util.ErrorUtils
import com.slachdevm.mrrgmobile.domain.model.Notification

class NotificationRepository(
    private val api: NotificationApi
) {
    suspend fun getNotifications(): Result<List<Notification>> {
        return try {
            val response = api.getNotifications()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to load notifications")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<Long> {
        return try {
            val response = api.getUnreadCount()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to load unread count")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: Long): Result<Notification> {
        return try {
            val response = api.markAsRead(id)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to mark notification as read")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = api.markAllAsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to mark all notifications as read")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
