package com.slachdevm.mrrgmobile.data.dto

data class ActivateAccountRequestDto(
    val token: String,
    val password: String
)