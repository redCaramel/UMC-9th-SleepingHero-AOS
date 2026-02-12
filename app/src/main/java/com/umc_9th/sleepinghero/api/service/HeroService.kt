package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.HeroDetailDTO
import retrofit2.http.GET
import retrofit2.http.Header

interface HeroService {
    @GET("/characters/me")
    suspend fun getMyHero(
        @Header("Authorization") token: String
    ): ApiResponse<HeroDetailDTO>
}