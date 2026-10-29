package com.maria.aqua.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = repository.getDay()

                if (response.isSuccessful) {
                    val dayData = response.body()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayLogs = dayData?.logs
                                ?.map { log ->
                                    com.maria.aqua.WaterRecord(
                                        amount = log.amountMl,
                                        timestamp = System.currentTimeMillis()
                                    )
                                }
                                .orEmpty(),
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = HistoryError.SERVER)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = HistoryError.UNKNOWN)
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
