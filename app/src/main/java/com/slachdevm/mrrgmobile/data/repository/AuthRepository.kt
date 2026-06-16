package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.SessionManager
import com.slachdevm.mrrgmobile.data.api.AuthApi
import com.slachdevm.mrrgmobile.data.api.RetrofitClient
import com.slachdevm.mrrgmobile.data.dto.LoginRequestDto
import com.slachdevm.mrrgmobile.data.dto.LoginResponseDto
import com.slachdevm.mrrgmobile.domain.model.UserRole

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {

    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponseDto> {
        return try {
            val response = authApi.login(
                LoginRequestDto(
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    sessionManager.saveAuthToken(body.token)
                    sessionManager.saveUserName(body.name)
                    sessionManager.saveUserRole(body.role)
                    RetrofitClient.setAuthToken(body.token)
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun logout() {
        sessionManager.clearSession()
        RetrofitClient.setAuthToken(null)
    }

    fun isLoggedIn(): Boolean {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            RetrofitClient.setAuthToken(token)
            return true
        }
        return false
    }

    fun getUserName(): String? {
        return sessionManager.fetchUserName()
    }

    fun getUserRole(): UserRole? {
        return sessionManager.fetchUserRole()
    }
}
