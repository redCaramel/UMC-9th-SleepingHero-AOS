package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsPageDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SleepService {
    @GET("sleep-sessions")
    suspend fun getSleepSessions(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<SleepSessionsPageDto>
}