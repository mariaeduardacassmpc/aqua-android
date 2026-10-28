package com.maria.aqua.data.repository

import com.maria.aqua.data.remote.WaterApi

class WaterRepository(
    private val waterApi: WaterApi
) {
    suspend fun create(amount: Int) =
        waterApi.create(
            com.maria.aqua.CreateWaterLogRequest(amountMl = amount)
        )

    suspend fun getDay() = waterApi.getDay()

    suspend fun delete(id: Int) = waterApi.delete(id)
}
