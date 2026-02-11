package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.DashBoardResponse
import com.umc_9th.sleepinghero.api.dto.HeroDetailDTO

class HomeRepository {
    private val homeService = ApiClient.homeService

    suspend fun getDashboard(token: String): ApiResponse<DashBoardResponse> {
        return homeService.getDashboard(token)
    }
}
