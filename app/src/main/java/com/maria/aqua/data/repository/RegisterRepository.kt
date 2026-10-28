package com.maria.aqua.data.repository

import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.data.remote.model.auth.RegisterRequest

class RegisterRepository {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ) = ClienteApi.authApi.register(
        RegisterRequest(
            name = name,
            email = email,
            password = password
        )
    )
}
