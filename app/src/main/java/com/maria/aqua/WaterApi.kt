package com.maria.aqua

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

class WaterApi {

    data class CreateWaterLogRequest(
        val amountMl: Int,
        val consumedAt: String? = null
    )

    data class WaterLogResponse(
        val id: Int,
        val amountMl: Int,
        val consumedAt: String
    )

    data class DayHistoryResponse(
        val day: String,
        val totalMl: Int,
        val logs: List<WaterLogResponse>
    )

    data class DailyTotalResponse(
        val day: String,
        val totalMl: Int,
        val count: Int
    )

    interface WaterApi  {
        @POST("api/WaterLogs")
        suspend fun create(
            @Body request: CreateWaterLogRequest
        ): Response<WaterLogResponse>

        @DELETE("api/WaterLogs/{id}")
        suspend fun delete(
            @Path("id") id: Int
        ): Response<Unit>

        @GET("api/WaterLogs/day")
        suspend fun getDay(
            @Query("day") day: String? = null
        ): Response<DayHistoryResponse>

        @GET("api/WaterLogs/history")
        suspend fun getHistory(): Response<List<DailyTotalResponse>>
    }
}