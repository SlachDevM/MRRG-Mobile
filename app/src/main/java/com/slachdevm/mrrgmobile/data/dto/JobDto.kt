package com.slachdevm.mrrgmobile.data.dto

import com.slachdevm.mrrgmobile.domain.model.JobStatus

data class JobDto(
    val id: Long?,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val jobTypes: String,
    val status: JobStatus,
    val jobDate: String?,
    val jobStartHour: String?,
    val assignedWorkers: String?,
    val assignedWorkerDetails: List<AssignedWorkerDto>?,
    val details: String?,
    val beforePhotos: List<String>,
    val afterPhotos: List<String>,
    val notes: String?,
    val priorityLevel: Int,
    val createdBy: Long?,
    val createdAt: Long?,
    val updatedAt: Long?
)
