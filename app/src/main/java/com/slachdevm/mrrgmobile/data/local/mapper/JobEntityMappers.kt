package com.slachdevm.mrrgmobile.data.local.mapper

import com.slachdevm.mrrgmobile.data.local.entity.JobEntity
import com.slachdevm.mrrgmobile.domain.model.Job
import java.time.LocalDate

fun Job.toEntity(): JobEntity? {
    val jobId = id ?: return null

    return JobEntity(
        id = jobId,
        clientName = clientName,
        clientPhone = clientPhone,
        clientAddress = clientAddress,
        jobTypes = jobTypes,
        status = status,
        jobDate = jobDate?.toString(),
        jobStartHour = jobStartHour,
        assignedWorkers = assignedWorkers,
        assignedWorkerNames = assignedWorkerNames,
        details = details,
        beforePhotos = beforePhotos,
        afterPhotos = afterPhotos,
        notes = notes,
        priorityLevel = priorityLevel,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun JobEntity.toDomain(): Job {
    return Job(
        id = id,
        clientName = clientName,
        clientPhone = clientPhone,
        clientAddress = clientAddress,
        jobTypes = jobTypes,
        status = status,
        jobDate = jobDate?.let { LocalDate.parse(it) },
        jobStartHour = jobStartHour,
        assignedWorkers = assignedWorkers,
        assignedWorkerNames = assignedWorkerNames,
        details = details,
        beforePhotos = beforePhotos,
        afterPhotos = afterPhotos,
        notes = notes,
        priorityLevel = priorityLevel,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
