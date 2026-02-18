package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.HeroDetailDTO

class HeroRepository {
    private val heroService = ApiClient.heroService

    suspend fun getMyHero(accessToken: String): ApiResponse<HeroDetailDTO> {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        return heroService.getMyHero(token)
    }
}
