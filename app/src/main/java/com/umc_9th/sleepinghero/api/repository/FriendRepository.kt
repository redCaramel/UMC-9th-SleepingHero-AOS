package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.dto.FriendItem
import com.umc_9th.sleepinghero.api.dto.FriendRankingItem
import com.umc_9th.sleepinghero.api.dto.FriendRequestItem
import com.umc_9th.sleepinghero.api.dto.FriendRequestStatusRequest
import com.umc_9th.sleepinghero.api.dto.SendFriendRequestRequest
import com.umc_9th.sleepinghero.api.service.FriendService

class FriendRepository(private val friendService: FriendService) {

    suspend fun getFriendRequests(token: String): Result<List<FriendRequestItem>> {
        return try {
            val token = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = friendService.getFriendRequests(token)

            if (response.isSuccess) Result.success(response.result ?: emptyList())
            else Result.failure(Exception(response.message ?: "친구 요청 목록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendsList(token: String): Result<List<FriendItem>> {
        return try {
            val token = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = friendService.getFriendsList(token)

            if (response.isSuccess) Result.success(response.result ?: emptyList())
            else Result.failure(Exception(response.message ?: "친구 목록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendRanking(token: String): Result<List<FriendRankingItem>> {
        return try {
            val token = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = friendService.getFriendRanking(token)

            if (response.isSuccess) Result.success(response.result ?: emptyList())
            else Result.failure(Exception(response.message ?: "친구 랭킹 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveFriendRequest(token: String, nickname: String): Result<String> {
        return try {
            val token = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val request = FriendRequestStatusRequest(nickname = nickname)
            val response = friendService.updateFriendRequestStatus(token, "APPROVE", request)

            if (response.isSuccess) Result.success(response.result ?: "친구 요청 승인됨")
            else Result.failure(Exception(response.message ?: "친구 요청 승인 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectFriendRequest(token: String, nickname: String): Result<String> {
        return try {
            val token = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val request = FriendRequestStatusRequest(nickname = nickname)
            val response = friendService.updateFriendRequestStatus(token, "REJECTED", request)

            if (response.isSuccess) Result.success(response.result ?: "친구 요청 거절됨")
            else Result.failure(Exception(response.message ?: "친구 요청 거절 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(token: String, nickname: String): Result<String> {
        return try {
            val token = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val request = SendFriendRequestRequest(nickname = nickname)
            val response = friendService.sendFriendRequest(token, request)

            if (response.isSuccess) Result.success(response.result ?: "친구 요청 전송됨")
            else Result.failure(Exception(response.message ?: "친구 요청 전송 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
