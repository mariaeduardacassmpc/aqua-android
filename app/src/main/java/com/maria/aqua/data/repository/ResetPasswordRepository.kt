package com.maria.aqua.data.repository

import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.data.remote.model.auth.PasswordResetRequest

class ResetPasswordRepository {
    suspend fun resetPassword(code: String, newPassword: String) =
        ClienteApi.authApi.resetPassword(
            PasswordResetRequest(
                code = code,
                newPassword = newPassword
            )
        )
}
