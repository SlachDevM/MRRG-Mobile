package com.slachdevm.mrrgmobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(LoginUiState())
    val uiStateFlow: StateFlow<LoginUiState> = _uiStateFlow.asStateFlow()

    fun onEmailChange(email: String) {
        _uiStateFlow.value = _uiStateFlow.value.copy(
            email = email,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiStateFlow.value = _uiStateFlow.value.copy(
            password = password,
            errorMessage = null
        )
    }

    fun login() {
        val currentState = _uiStateFlow.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiStateFlow.value = currentState.copy(
                errorMessage = "Email and password are required"
            )
            return
        }

        viewModelScope.launch {
            _uiStateFlow.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.login(
                email = currentState.email,
                password = currentState.password
            )

            result
                .onSuccess {
                    _uiStateFlow.value = _uiStateFlow.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiStateFlow.value = _uiStateFlow.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
        }
    }
}
