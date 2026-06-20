package com.slachdevm.mrrgmobile.data.dto

import com.slachdevm.mrrgmobile.domain.model.Job
import java.time.LocalDate

fun JobDto.toDomain(): Job {
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

fun Job.toDto(): JobDto {
    return JobDto(
        id = id,
        clientName = clientName,
        clientPhone = clientPhone,
        clientAddress = clientAddress,
        jobTypes = jobTypes,
        status = status,
        jobDate = jobDate?.toString(),
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
