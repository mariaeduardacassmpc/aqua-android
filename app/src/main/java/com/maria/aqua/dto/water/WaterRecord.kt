package com.maria.aqua

data class WaterRecord(
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis()
)