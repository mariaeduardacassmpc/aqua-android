package com.maria.aqua.ui.main

data class MainUiState(
    val dailyGoal: Int = 2000,
    val consumedWater: Int = 0,
    val canUndo: Boolean = false,
    val isLoading: Boolean = false
)

sealed class MainUiEvent {
    data class Message(val text: String) : MainUiEvent()
    data class WaterAdded(val amount: Int) : MainUiEvent()
}
