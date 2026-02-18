package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.SkinListDTO

class SkinRepository {
    private val skinService = ApiClient.skinService

    suspend fun getMySkins(accessToken: String): ApiResponse<SkinListDTO> {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        return skinService.getMySkins(token)
    }
}