
package com.maria.aqua

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class PasswordResetRequest(
    val email: String,
    val token: String,
    val newPassword: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val email: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: UserResponse
)

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
    ): retrofit2.Response<Unit>
}
