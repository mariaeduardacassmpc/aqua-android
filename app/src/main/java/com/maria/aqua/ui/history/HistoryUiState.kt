package com.maria.aqua.ui.history

import com.maria.aqua.DailyTotalResponse
import com.maria.aqua.WaterRecord

enum class HistoryError {
    NETWORK,
    SERVER,
    UNKNOWN
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val days: List<DailyTotalResponse> = emptyList(),
    val todayLogs: List<WaterRecord> = emptyList(),
    val error: HistoryError? = null
) {
    val totalMl: Int
        get() = days.sumOf { it.totalMl }

    val averageMl: Int
        get() = if (days.isEmpty()) 0 else totalMl / days.size
}
