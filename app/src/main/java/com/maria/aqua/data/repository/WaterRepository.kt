package com.maria.aqua.data.repository

import com.maria.aqua.WaterApi
import com.maria.aqua.WaterLogResponse
import com.maria.aqua.domain.model.DayTotal
import com.maria.aqua.domain.model.HistorySummary
import com.maria.aqua.domain.model.WaterLog
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException

class WaterRepository(private val api: WaterApi) {

    suspend fun getHistory(today: LocalDate, daysCount: Int = 5): Result<HistorySummary> =
        suspendRunCatching {
            coroutineScope {
                val from = today.minusDays(daysCount - 1L)
                val dayCall = async { api.getDay() }
                val historyCall = async { api.getHistory(from.toString(), today.toString()) }

                val dayBody = dayCall.await().bodyOrThrow()
                val historyBody = historyCall.await().bodyOrThrow()

                val totals = historyBody
                    .associate { it.day.take(10).let(LocalDate::parse) to it.totalMl }
                    .toMutableMap()
                totals[today] = dayBody.totalMl

                HistorySummary(
                    todayLogs = dayBody.logs
                        .map { it.toDomain() }
                        .sortedByDescending { it.consumedAt },
                    days = ((daysCount - 1) downTo 0).map { offset ->
                        val date = today.minusDays(offset.toLong())
                        DayTotal(date, totals[date] ?: 0)
                    }
                )
            }
        }
}

private fun WaterLogResponse.toDomain() = WaterLog(id, amountMl, parseDateTime(consumedAt))

internal fun parseDateTime(value: String): LocalDateTime {
    val normalized = value.trim().replace(' ', 'T')
    return runCatching {
        OffsetDateTime.parse(normalized)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    }.recoverCatching {
        LocalDateTime.parse(normalized)
    }.getOrElse { LocalDateTime.now() }
}

private fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return body() ?: throw IllegalStateException("Resposta vazia da API")
}

// runCatching comum engole CancellationException e quebra o cancelamento de coroutines
private suspend inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }