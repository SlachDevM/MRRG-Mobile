package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.api.UserApi
import com.slachdevm.mrrgmobile.data.dto.UserProfileDto
import com.slachdevm.mrrgmobile.data.local.dao.UserProfileDao
import com.slachdevm.mrrgmobile.data.local.mapper.toDomain
import com.slachdevm.mrrgmobile.data.local.mapper.toEntity
import com.slachdevm.mrrgmobile.data.util.ErrorUtils

class ProfileRepository(
    private val userApi: UserApi,
    private val userProfileDao: UserProfileDao
) {
    suspend fun getCurrentUser(): Result<UserProfileDto> {
        return try {
            val response = userApi.getCurrentUser()

            val body = response.body()
            if (response.isSuccessful && body != null) {
                val remoteProfile = body

                userProfileDao.upsertProfile(remoteProfile.toEntity())

                Result.success(remoteProfile)
            } else {
                val cachedProfile = userProfileDao.getProfile()?.toDomain()

                if (cachedProfile != null) {
                    Result.success(cachedProfile)
                } else {
                    val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to fetch profile")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            val cachedProfile = userProfileDao.getProfile()?.toDomain()

            if (cachedProfile != null) {
                Result.success(cachedProfile)
            } else {
                Result.failure(e)
            }
        }
    }
}
