package com.slachdevm.mrrgmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slachdevm.mrrgmobile.domain.model.JobStatus

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: Long,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val jobTypes: String,
    val status: JobStatus,
    val jobDate: String?,
    val jobStartHour: String?,
    val assignedWorkers: String?,
    val assignedWorkerNames: String?,
    val details: String?,
    val beforePhotos: List<String>,
    val afterPhotos: List<String>,
    val notes: String?,
    val priorityLevel: Int,
    val createdBy: Long?,
    val createdAt: Long?,
    val updatedAt: Long?
)