package com.slachdevm.mrrgmobile.data.api

import com.slachdevm.mrrgmobile.domain.model.Notification
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationApi {

    @GET("/api/notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @GET("/api/notifications/unread")
    suspend fun getUnreadNotifications(): Response<List<Notification>>

    @GET("/api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<Long>

    @PUT("/api/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Long
    ): Response<Notification>

    @PUT("/api/notifications/read-all")
    suspend fun markAllAsRead(): Response<String>
}