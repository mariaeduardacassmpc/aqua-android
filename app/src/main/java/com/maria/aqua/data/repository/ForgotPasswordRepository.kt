package com.maria.aqua.data.repository

import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.data.remote.model.auth.ForgotPasswordRequest

class ForgotPasswordRepository {
    suspend fun forgotPassword(email: String) =
        ClienteApi.authApi.forgotPassword(
            ForgotPasswordRequest(email = email)
        )
}