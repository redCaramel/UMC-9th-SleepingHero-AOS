package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.FriendItem
import com.umc_9th.sleepinghero.api.dto.FriendRankingItem
import com.umc_9th.sleepinghero.api.dto.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.FriendRequestItem
import com.umc_9th.sleepinghero.api.dto.FriendRequestStatusRequest
import com.umc_9th.sleepinghero.api.dto.FriendRequestsResponse
import com.umc_9th.sleepinghero.api.dto.FriendsListResponse
import com.umc_9th.sleepinghero.api.dto.SendFriendRequestRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendService {

    /**
     * 대기 중인 친구 요청 목록 조회
     * GET /friends/requests
     *
     * Response:
     * [
     *   {
     *     "memberId": 0,
     *     "nickname": "string",
     *     "profilePicture": "string"
     *   }
     * ]
     */
    @GET("/friends/requests")
    suspend fun getFriendRequests(
        @Header("Authorization") token: String
    ): ApiResponse<List<FriendRequestItem>>

    /**
     * 친구 목록 조회 (승인된 친구)
     * GET /friends
     *
     * Response:
     * [
     *   {
     *     "memberId": 0,
     *     "nickname": "string",
     *     "profilePicture": "string"
     *   }
     * ]
     */
    @GET("/friends")
    suspend fun getFriendsList(
        @Header("Authorization") token: String
    ): ApiResponse<List<FriendItem>>

    /**
     * 친구 수면 랭킹 조회
     * GET /friends/ranking
     *
     * Response:
     * [
     *   {
     *     "nickname": "string",
     *     "totalSleepTime": "string",
     *     "rank": 0
     *   }
     * ]
     */
    @GET("/friends/ranking")
    suspend fun getFriendRanking(
        @Header("Authorization") token: String
    ): ApiResponse<List<FriendRankingItem>>

    /**
     * 친구 요청 수락/거절
     * PATCH /friends/requests/{status}
     *
     * @param status "APPROVE" 또는 "REJECTED"
     *
     * Request Body:
     * {
     *   "nickname": "string"
     * }
     */
    @PATCH("/friends/requests/{status}")
    suspend fun updateFriendRequestStatus(
        @Header("Authorization") token: String,
        @Path("status") status: String,
        @Body request: FriendRequestStatusRequest
    ): ApiResponse<String>

    /**
     * 친구 요청 보내기
     * POST /friends/requests
     *
     * Request Body:
     * {
     *   "nickname": "string"
     * }
     */
    @POST("/friends/requests")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body request: SendFriendRequestRequest
    ): ApiResponse<String>
}