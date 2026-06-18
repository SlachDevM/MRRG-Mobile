package com.slachdevm.mrrgmobile.ui.profile

import com.slachdevm.mrrgmobile.data.dto.UserProfileDto

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserProfileDto? = null,
    val errorMessage: String? = null
)