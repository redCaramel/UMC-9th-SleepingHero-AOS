package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.CreateHeroResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface CreateService {
    @POST("/characters")
    suspend fun CreateHero(
        @Header("Authorization") token: String
    ) : Response<ApiResponse<CreateHeroResponse>>

}