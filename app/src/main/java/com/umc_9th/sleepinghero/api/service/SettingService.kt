package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.FAQResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SettingService {
    @POST("/help/inquiries")
    suspend fun FAQUrl(
        @Header("Authorization") token: String
    ) : ApiResponse<FAQResponse>
}