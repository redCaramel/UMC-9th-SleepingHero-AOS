package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.FAQRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SettingService {
    @POST("/help/inquiry")
    suspend fun FAQUrl(
        @Header("Authorization") token: String,
        @Body req : FAQRequest
    ) : Response<ApiResponse<String>>
}