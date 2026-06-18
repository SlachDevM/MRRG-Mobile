package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.api.UserApi
import com.slachdevm.mrrgmobile.data.dto.UserProfileDto

class ProfileRepository(
    private val userApi: UserApi
) {
    suspend fun getCurrentUser(): Result<UserProfileDto> {
        return try {
            val response = userApi.getCurrentUser()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty profile response"))
                }
            } else {
                Result.failure(Exception("Failed to load profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}