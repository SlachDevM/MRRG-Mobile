package com.slachdevm.mrrgmobile.ui.auth

data class ActivateAccountUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isActivated: Boolean = false,
    val errorMessage: String? = null
)