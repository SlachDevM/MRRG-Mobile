package com.slachdevm.mrrgmobile.domain.model

data class Job(
    val id: Long? = null,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val jobTypes: String,
    val status: JobStatus = JobStatus.PENDING,
    val jobDate: Long? = null,
    val jobStartHour: String? = null,
    val assignedWorkers: String? = null,
    val details: String? = null,
    val beforePhotos: List<String> = emptyList(),
    val afterPhotos: List<String> = emptyList(),
    val notes: String? = null,
    val priorityLevel: Int = 1,
    val createdBy: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
