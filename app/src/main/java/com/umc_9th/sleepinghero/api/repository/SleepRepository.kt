package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsPageDto

class SleepRepository {
    private val sleepService = ApiClient.sleepService

    suspend fun getSleepSessions(token: String, page: Int, size: Int): ApiResponse<SleepSessionsPageDto> {
        return sleepService.getSleepSessions(token, page, size)
    }
}
