package com.slachdevm.mrrgmobile.data.api

import com.slachdevm.mrrgmobile.data.model.FcmTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface UserApi {

    @PUT("/api/users/me/fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenRequest
    ): Response<Unit>
}