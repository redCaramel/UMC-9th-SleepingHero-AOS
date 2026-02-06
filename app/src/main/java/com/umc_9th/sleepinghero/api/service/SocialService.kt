package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.MyCharResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header

interface SocialService {
    @GET("/characters/search")
    suspend fun CharacterSearch(
        @Header("Authorization") token: String,
        @Body req : CharSearchRequest
    ) : Response<ApiResponse<CharSearchResponse>>

    @GET("/characters/me")
    suspend fun MyCharacter(
        @Header("Authorization") token: String
    ) : Response<ApiResponse<MyCharResponse>>
}