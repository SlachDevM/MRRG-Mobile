package com.slachdevm.mrrgmobile.data.dto

import com.slachdevm.mrrgmobile.domain.model.UserRole

data class LoginResponseDto(
    val userId: Long,
    val email: String,
    val name: String,
    val role: UserRole,
    val token: String
)
