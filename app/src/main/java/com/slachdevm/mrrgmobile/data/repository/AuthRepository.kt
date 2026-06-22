package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.session.SessionManager
import com.slachdevm.mrrgmobile.data.api.AuthApi
import com.slachdevm.mrrgmobile.data.api.RetrofitClient
import com.slachdevm.mrrgmobile.data.api.UserApi
import com.slachdevm.mrrgmobile.data.dto.LoginRequestDto
import com.slachdevm.mrrgmobile.data.dto.LoginResponseDto
import com.slachdevm.mrrgmobile.data.dto.ActivateAccountRequestDto
import com.slachdevm.mrrgmobile.data.model.FcmTokenRequest
import com.slachdevm.mrrgmobile.domain.model.UserRole
import com.google.gson.JsonParser
import retrofit2.Response

class AuthRepository(
    private val authApi: AuthApi,
    private val userApi: UserApi,
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
                    sessionManager.saveUserId(body.userId)
                    sessionManager.saveUserName(body.name)
                    sessionManager.saveUserRole(body.role)
                    RetrofitClient.setAuthToken(body.token)
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorMessage = extractErrorMessage(response, "Incorrect email or password")
                Result.failure(Exception(errorMessage))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            val response = userApi.updateFcmToken(FcmTokenRequest(token))

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update FCM token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateAccount(
        token: String,
        password: String
    ): Result<Unit> {
        return try {
            val response = authApi.activateAccount(
                ActivateAccountRequestDto(
                    token = token,
                    password = password
                )
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Account activation failed")))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun extractErrorMessage(response: Response<*>, fallbackMessage: String): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                fallbackMessage
            } else if (errorBody.trim().startsWith("{")) {
                val jsonObject = JsonParser.parseString(errorBody).asJsonObject
                jsonObject.get("message")?.asString ?: fallbackMessage
            } else {
                errorBody
            }
        } catch (exception: Exception) {
            fallbackMessage
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

    fun getUserId(): Long {
        return sessionManager.fetchUserId()
    }

    fun getUserRole(): UserRole? {
        return sessionManager.fetchUserRole()
    }
}
