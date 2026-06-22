package com.slachdevm.mrrgmobile.domain.model

import java.time.LocalDate

data class Job(
    val id: Long? = null,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val jobTypes: String,
    val status: JobStatus = JobStatus.PENDING,
    val jobDate: LocalDate? = null,
    val jobStartHour: String? = null,
    val assignedWorkers: String? = null,
    val assignedWorkerNames: String? = null,
    val details: String? = null,
    val beforePhotos: List<String> = emptyList(),
    val afterPhotos: List<String> = emptyList(),
    val notes: String? = null,
    val priorityLevel: Int = 1,
    val createdBy: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
