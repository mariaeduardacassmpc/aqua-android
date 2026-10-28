package com.maria.aqua

data class CreateWaterLogRequest(
    val amountMl: Int,
    val consumedAt: String? = null
)