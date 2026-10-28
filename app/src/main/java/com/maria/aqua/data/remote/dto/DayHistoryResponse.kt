package com.maria.aqua

data class DayHistoryResponse(
    val day: String,
    val totalMl: Int,
    val logs: List<WaterLogResponse>
)