package com.slachdevm.mrrgmobile.data.local.mapper

import com.slachdevm.mrrgmobile.data.local.entity.UserProfileEntity
import com.slachdevm.mrrgmobile.data.dto.UserProfileDto

fun UserProfileDto.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        email = email,
        role = role
    )
}

fun UserProfileEntity.toDomain(): UserProfileDto {
    return UserProfileDto(
        id = id,
        name = name,
        email = email,
        role = role
    )
}