package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.MyFriendFragment
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.ChangeNameRequest
import com.umc_9th.sleepinghero.api.dto.ChangeNameResponse
import com.umc_9th.sleepinghero.api.dto.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.FriendInviteRequest
import com.umc_9th.sleepinghero.api.dto.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.MyCharResponse
import com.umc_9th.sleepinghero.api.dto.MyFriendResponse
import com.umc_9th.sleepinghero.api.dto.RequestCheckResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SocialService {
    @GET("/characters/search")
    suspend fun CharacterSearch(
        @Header("Authorization") token: String,
        @Query("name") name : String
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

    @GET("/friends")
    suspend fun MyFriends(
        @Header("Authorization") token : String
    ) : Response<ApiResponse<List<MyFriendResponse>>>

    @POST("/friends/requests")
    suspend fun FriendInvite(
        @Header("Authorization") token : String,
        @Body req : FriendInviteRequest
    ) : Response<ApiResponse<String>>

    @GET("/friends/requests")
    suspend fun CheckRequest(
        @Header("Authorization") token : String,
    ): Response<ApiResponse<List<RequestCheckResponse>>>
}