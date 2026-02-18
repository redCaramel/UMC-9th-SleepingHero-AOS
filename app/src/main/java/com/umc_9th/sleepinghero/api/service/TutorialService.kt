package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.TutorialStatusResponse
import com.umc_9th.sleepinghero.api.dto.TutorialUpdateRequest
import com.umc_9th.sleepinghero.api.dto.TutorialUpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH

interface TutorialService {

    @GET("/users/me/tutorial")
    suspend fun getTutorial(
        @Header("Authorization") token: String
    ): retrofit2.Response<ApiResponse<TutorialStatusResponse>>

    @PATCH("/users/me/tutorial")
    suspend fun patchTutorial(
        @Header("Authorization") token: String,
        @Body body: TutorialUpdateRequest
    ): retrofit2.Response<ApiResponse<TutorialUpdateResponse>>
}