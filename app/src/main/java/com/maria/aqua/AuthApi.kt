package com.maria.aqua

import com.google.gson.JsonObject
import com.maria.aqua.dto.auth.ForgotPasswordRequest
import com.maria.aqua.dto.auth.LoginRequest
import com.maria.aqua.dto.auth.LoginResponse
import com.maria.aqua.dto.auth.PasswordResetRequest
import com.maria.aqua.dto.auth.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/Auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<JsonObject>

    @POST("api/Auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<Unit>

    @POST("api/Auth/reset-password")
    suspend fun resetPassword(
        @Body request: PasswordResetRequest
    ): Response<Unit>
}