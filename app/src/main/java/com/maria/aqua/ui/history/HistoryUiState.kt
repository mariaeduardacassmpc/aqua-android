package com.maria.aqua.ui.history

import com.maria.aqua.domain.model.DayTotal
import com.maria.aqua.domain.model.WaterLog

enum class HistoryError { NETWORK, SERVER, UNKNOWN }

data class HistoryUiState(
    val isLoading: Boolean = true,
    val days: List<DayTotal> = emptyList(),
    val todayLogs: List<WaterLog> = emptyList(),
    val error: HistoryError? = null
) {
    val totalMl: Int get() = days.sumOf { it.totalMl }
    val averageMl: Int get() = if (days.isEmpty()) 0 else totalMl / days.size
}