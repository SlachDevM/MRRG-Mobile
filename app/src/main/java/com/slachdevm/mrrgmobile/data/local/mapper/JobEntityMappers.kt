package com.slachdevm.mrrgmobile.data.local.mapper

import com.slachdevm.mrrgmobile.data.local.entity.JobEntity
import com.slachdevm.mrrgmobile.domain.model.Job

fun Job.toEntity(): JobEntity? {
    val jobId = id ?: return null

    return JobEntity(
        id = jobId,
        clientName = clientName,
        clientPhone = clientPhone,
        clientAddress = clientAddress,
        jobTypes = jobTypes,
        status = status,
        jobDate = jobDate,
        jobStartHour = jobStartHour,
        assignedWorkers = assignedWorkers,
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
        jobDate = jobDate,
        jobStartHour = jobStartHour,
        assignedWorkers = assignedWorkers,
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