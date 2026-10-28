package com.maria.aqua.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

class HistoryViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun load(today: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getHistory(today)
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            days = summary.days,
                            todayLogs = summary.todayLogs,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toHistoryError()) }
                }
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(error = null) }
}

private fun Throwable.toHistoryError() = when (this) {
    is IOException -> HistoryError.NETWORK
    is HttpException -> HistoryError.SERVER
    else -> HistoryError.UNKNOWN
}