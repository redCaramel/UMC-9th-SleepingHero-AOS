package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.ChangeNameRequest
import com.umc_9th.sleepinghero.api.dto.ChangeNameResponse
import com.umc_9th.sleepinghero.api.dto.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.MyCharResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH

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

    @GET("/friends/ranking")
    suspend fun FriendRanking(
        @Header("Authorization") token: String
    ) : Response<ApiResponse<List<FriendRankingResponse>>>


    @PATCH("/characters/name")
    suspend fun ChangeName(
        @Header("Authorization") token: String,
        @Body req : ChangeNameRequest
    ) : Response<ApiResponse<ChangeNameResponse>>
}