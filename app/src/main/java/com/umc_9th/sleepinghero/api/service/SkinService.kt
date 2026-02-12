package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.SkinListDTO
import retrofit2.http.GET
import retrofit2.http.Header

interface SkinService {
    @GET("wardrobe/me/skins")
    suspend fun getMySkins(
        @Header("Authorization") token: String
    ): ApiResponse<SkinListDTO>
}