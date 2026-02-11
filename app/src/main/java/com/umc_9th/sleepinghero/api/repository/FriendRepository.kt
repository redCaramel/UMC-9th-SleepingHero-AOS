package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.dto.FriendItem
import com.umc_9th.sleepinghero.api.dto.FriendRankingItem
import com.umc_9th.sleepinghero.api.dto.FriendRequestItem
import com.umc_9th.sleepinghero.api.dto.FriendRequestStatusRequest
import com.umc_9th.sleepinghero.api.dto.SendFriendRequestRequest
import com.umc_9th.sleepinghero.api.service.FriendService

class FriendRepository(private val friendService: FriendService) {

    /**
     * 대기 중인 친구 요청 목록 조회
     * GET /friends/requests
     */
    suspend fun getFriendRequests(token: String): Result<List<FriendRequestItem>> {
        return try {
            val response = friendService.getFriendRequests("Bearer $token")

            if (response.isSuccess) {
                Result.success(response.result ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "친구 요청 목록 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 친구 목록 조회 (승인된 친구)
     * GET /friends
     */
    suspend fun getFriendsList(token: String): Result<List<FriendItem>> {
        return try {
            val response = friendService.getFriendsList("Bearer $token")

            if (response.isSuccess) {
                Result.success(response.result ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "친구 목록 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 친구 수면 랭킹 조회
     * GET /friends/ranking
     */
    suspend fun getFriendRanking(token: String): Result<List<FriendRankingItem>> {
        return try {
            val response = friendService.getFriendRanking("Bearer $token")

            if (response.isSuccess) {
                Result.success(response.result ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "친구 랭킹 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 친구 요청 수락
     * PATCH /friends/requests/APPROVE
     */
    suspend fun approveFriendRequest(token: String, nickname: String): Result<String> {
        return try {
            val request = FriendRequestStatusRequest(nickname = nickname)
            val response = friendService.updateFriendRequestStatus("Bearer $token", "APPROVE", request)

            if (response.isSuccess) {
                Result.success(response.result ?: "친구 요청 승인됨")
            } else {
                Result.failure(Exception(response.message ?: "친구 요청 승인 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 친구 요청 거절
     * PATCH /friends/requests/REJECTED
     */
    suspend fun rejectFriendRequest(token: String, nickname: String): Result<String> {
        return try {
            val request = FriendRequestStatusRequest(nickname = nickname)
            val response = friendService.updateFriendRequestStatus("Bearer $token", "REJECTED", request)

            if (response.isSuccess) {
                Result.success(response.result ?: "친구 요청 거절됨")
            } else {
                Result.failure(Exception(response.message ?: "친구 요청 거절 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 친구 요청 보내기
     * POST /friends/requests
     */
    suspend fun sendFriendRequest(token: String, nickname: String): Result<String> {
        return try {
            val request = SendFriendRequestRequest(nickname = nickname)
            val response = friendService.sendFriendRequest("Bearer $token", request)

            if (response.isSuccess) {
                Result.success(response.result ?: "친구 요청 전송됨")
            } else {
                Result.failure(Exception(response.message ?: "친구 요청 전송 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}