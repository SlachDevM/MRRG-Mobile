package com.slachdevm.mrrgmobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivateAccountViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(ActivateAccountUiState())
    val uiStateFlow: StateFlow<ActivateAccountUiState> = _uiStateFlow.asStateFlow()

    fun onPasswordChange(password: String) {
        _uiStateFlow.value = _uiStateFlow.value.copy(
            password = password,
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiStateFlow.value = _uiStateFlow.value.copy(
            confirmPassword = confirmPassword,
            errorMessage = null
        )
    }

    fun validateToken(token: String?) {
        if (token.isNullOrBlank()) {
            _uiStateFlow.value = _uiStateFlow.value.copy(
                isValidatingToken = false,
                isTokenValid = false,
                errorMessage = "Activation link is invalid"
            )
            return
        }

        viewModelScope.launch {
            _uiStateFlow.value = _uiStateFlow.value.copy(
                isValidatingToken = true,
                errorMessage = null
            )

            val result = authRepository.validateActivationToken(token)

            result
                .onSuccess {
                    _uiStateFlow.value = _uiStateFlow.value.copy(
                        isValidatingToken = false,
                        isTokenValid = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiStateFlow.value = _uiStateFlow.value.copy(
                        isValidatingToken = false,
                        isTokenValid = false,
                        errorMessage = exception.message ?: "Invalid activation token"
                    )
                }
        }
    }

    fun activateAccount(token: String?) {
        val currentState = _uiStateFlow.value

        if (token.isNullOrBlank()) {
            _uiStateFlow.value = currentState.copy(
                errorMessage = "Activation link is invalid"
            )
            return
        }

        if (currentState.password.isBlank() || currentState.confirmPassword.isBlank()) {
            _uiStateFlow.value = currentState.copy(
                errorMessage = "Password and confirmation are required"
            )
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiStateFlow.value = currentState.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }

        viewModelScope.launch {
            _uiStateFlow.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.activateAccount(
                token = token,
                password = currentState.password
            )

            result
                .onSuccess {
                    _uiStateFlow.value = _uiStateFlow.value.copy(
                        isLoading = false,
                        isActivated = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiStateFlow.value = _uiStateFlow.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Account activation failed"
                    )
                }
        }
    }
}