package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.social.ChangeNameRequest
import com.umc_9th.sleepinghero.api.dto.social.ChangeNameResponse
import com.umc_9th.sleepinghero.api.dto.social.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.social.CheckSkinResponse
import com.umc_9th.sleepinghero.api.dto.social.DeleteFriendRequest
import com.umc_9th.sleepinghero.api.dto.social.FriendInviteRequest
import com.umc_9th.sleepinghero.api.dto.social.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.social.FriendRequestStatusUpdate
import com.umc_9th.sleepinghero.api.dto.social.MyCharResponse
import com.umc_9th.sleepinghero.api.dto.social.MyFriendResponse
import com.umc_9th.sleepinghero.api.dto.social.RequestCheckResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
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
        @Header("Authorization") token : String
    ): Response<ApiResponse<List<RequestCheckResponse>>>

    @PATCH("friends/requests/{status}")
    suspend fun updateFriendRequest(
        @Header("Authorization") token: String,
        @Path("status") status: String,
        @Body body: FriendRequestStatusUpdate
    ): Response<ApiResponse<String>>

    @GET("/wardrobe/me/skins")
    suspend fun CheckSkin(
        @Header("Authorization") token : String
    ) : Response<ApiResponse<CheckSkinResponse>>

    @POST("/wardrobe/me/skins/{skinId}/equip")
    suspend fun EquipSKin (
        @Header("Authorization") token : String,
        @Path("skinId") skinId : Int
    ) : Response<ApiResponse<String>>

    @HTTP(method = "DELETE", path = "/friends", hasBody = true)
    suspend fun DeleteFriend (
        @Header("Authorization") token : String,
        @Body nickName: DeleteFriendRequest
    ) : Response<ApiResponse<String>>
}