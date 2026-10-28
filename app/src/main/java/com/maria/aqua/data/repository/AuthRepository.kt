
package com.maria.aqua.data.repository

import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.data.remote.model.auth.LoginRequest

class AuthRepository {

    suspend fun login(email: String, password: String) =
        ClienteApi.authApi.login(
            LoginRequest(
                email = email,
                password = password
            )
        )
}
