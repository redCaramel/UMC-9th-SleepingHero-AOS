package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.CreateHeroResponse
import com.umc_9th.sleepinghero.api.dto.GroupCreateRequest
import com.umc_9th.sleepinghero.api.dto.GroupRankingInsideResponse
import com.umc_9th.sleepinghero.api.dto.GroupRankingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface GroupService {
    @POST("/groups")
    suspend fun CreateGroup(
        @Header("Authorization") token: String,
        @Body req : GroupCreateRequest
    ) : Response<ApiResponse<String>>

    @GET("/groups/rankings")
    suspend fun CheckGroup(
        @Header("Authorization") token: String
    ) : Response<ApiResponse<List<GroupRankingResponse>>>

    @GET("/groups/ranking/inside")
    suspend fun GroupRanking(
        @Header("Authorization") token: String,
        @Query("groupName") groupName : String
    ) : Response<ApiResponse<GroupRankingInsideResponse>>
}