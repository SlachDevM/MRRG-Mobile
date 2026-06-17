package com.slachdevm.mrrgmobile.domain.model

import com.google.gson.annotations.SerializedName

data class Notification(
    val id: Long,
    val userId: Long,
    val jobId: Long?,
    val type: String,
    val message: String,

    @SerializedName("isRead")
    val read: Boolean,

    val createdAt: Long?
)