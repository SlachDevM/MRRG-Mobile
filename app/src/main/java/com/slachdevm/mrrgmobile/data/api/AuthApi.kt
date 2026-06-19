package com.slachdevm.mrrgmobile.data.api

import com.slachdevm.mrrgmobile.data.dto.ActivateAccountRequestDto
import com.slachdevm.mrrgmobile.data.dto.LoginRequestDto
import com.slachdevm.mrrgmobile.data.dto.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    @POST("/api/auth/activate-account")
    suspend fun activateAccount(
        @Body request: ActivateAccountRequestDto
    ): Response<Unit>
}