package com.maria.aqua.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.WaterHistory
import com.maria.aqua.WaterLogResponse
import com.maria.aqua.WaterRecord
import com.maria.aqua.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>()
    val events = _events.asSharedFlow()

    private val waterLogs = mutableListOf<WaterLogResponse>()

    init {
        loadTodayWater()
    }

    fun addWater(amount: Int) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val response = repository.create(amount)

                if (response.isSuccessful) {
                    val log = response.body()

                    if (log == null) {
                        _events.emit(
                            MainUiEvent.Message("A API não retornou o registro.")
                        )
                        return@launch
                    }

                    waterLogs.add(log)

                    _uiState.value = _uiState.value.copy(
                        consumedWater = _uiState.value.consumedWater + log.amountMl,
                        canUndo = true
                    )

                    WaterHistory.records.add(
                        WaterRecord(
                            amount = log.amountMl,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    _events.emit(MainUiEvent.WaterAdded(log.amountMl))
                } else {
                    _events.emit(
                        MainUiEvent.Message(
                            "Não foi possível salvar (${response.code()})."
                        )
                    )
                }
            } catch (e: Exception) {
                _events.emit(
                    MainUiEvent.Message(
                        "Erro ao salvar o consumo. Verifique a conexão com a API."
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun undoLastWater() {
        val lastLog = waterLogs.lastOrNull()

        if (lastLog == null) {
            viewModelScope.launch {
                _events.emit(
                    MainUiEvent.Message(
                        "Não há registros adicionados nesta sessão para desfazer."
                    )
                )
            }
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val response = repository.delete(lastLog.id)

                if (response.isSuccessful) {
                    waterLogs.removeAt(waterLogs.lastIndex)

                    _uiState.value = _uiState.value.copy(
                        consumedWater = (
                                _uiState.value.consumedWater - lastLog.amountMl
                                ).coerceAtLeast(0),
                        canUndo = waterLogs.isNotEmpty()
                    )

                    val index = WaterHistory.records.indexOfLast {
                        it.amount == lastLog.amountMl
                    }

                    if (index >= 0) {
                        WaterHistory.records.removeAt(index)
                    }

                    _events.emit(
                        MainUiEvent.Message("${lastLog.amountMl} ml removidos!")
                    )
                } else {
                    _events.emit(
                        MainUiEvent.Message(
                            "Não foi possível remover (${response.code()})."
                        )
                    )
                }
            } catch (e: Exception) {
                _events.emit(
                    MainUiEvent.Message(
                        "Erro ao remover o consumo. Verifique a conexão com a API."
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadTodayWater() {
        viewModelScope.launch {
            try {
                val response = repository.getDay()

                if (response.isSuccessful) {
                    val dayData = response.body() ?: return@launch

                    waterLogs.clear()
                    waterLogs.addAll(dayData.logs)

                    WaterHistory.records.clear()
                    WaterHistory.records.addAll(
                        dayData.logs.map { log ->
                            WaterRecord(
                                amount = log.amountMl,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    )

                    _uiState.value = _uiState.value.copy(
                        consumedWater = dayData.totalMl,
                        canUndo = waterLogs.isNotEmpty()
                    )
                } else {
                    _events.emit(
                        MainUiEvent.Message(
                            "Erro ao carregar consumo: ${response.code()}"
                        )
                    )
                }
            } catch (e: Exception) {
                _events.emit(
                    MainUiEvent.Message(
                        "Não foi possível carregar o consumo salvo."
                    )
                )
            }
        }
    }
}
