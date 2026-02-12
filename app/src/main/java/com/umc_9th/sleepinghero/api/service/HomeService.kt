package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.DashBoardResponse

import retrofit2.http.GET
import retrofit2.http.Header

interface HomeService {
  
    @GET("home/dashboard")
    suspend fun getDashboard(
        @Header("Authorization") token: String
    ): ApiResponse<DashBoardResponse>

}