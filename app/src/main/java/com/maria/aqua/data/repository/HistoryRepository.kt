package com.maria.aqua.data.repository

import com.maria.aqua.DailyTotalResponse
import com.maria.aqua.WaterRecord
import com.maria.aqua.data.remote.ClienteApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
import java.util.TimeZone

data class HistorySummary(
    val days: List<DailyTotalResponse>,
    val todayLogs: List<WaterRecord>
)

class HistoryRepository {

    suspend fun getHistory(today: LocalDate = LocalDate.now()): Result<HistorySummary> {
        return try {
            val from = today.minusDays(4).toString()
            val to = today.toString()

            val dayResponse = ClienteApi.waterApi.getDay()
            val historyResponse = ClienteApi.waterApi.getHistory(from, to)

            if (!dayResponse.isSuccessful || !historyResponse.isSuccessful) {
                return Result.failure(
                    Exception("Erro ao carregar histórico")
                )
            }

            val dayData = dayResponse.body()
                ?: return Result.failure(Exception("Resposta do dia vazia"))

            val days = historyResponse.body().orEmpty().toMutableList()

            val todayLogs = dayData.logs.map { log ->
                WaterRecord(
                    amount = log.amountMl,
                    timestamp = parseTimestamp(log.consumedAt)
                )
            }

            Result.success(
                HistorySummary(
                    days = days,
                    todayLogs = todayLogs
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTimestamp(value: String): Long {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            try {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    if (pattern.endsWith("'Z'")) {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                }.parse(value.replace(" ", "T"))?.time
            } catch (_: Exception) {
                null
            }
        } ?: System.currentTimeMillis()
    }
}
