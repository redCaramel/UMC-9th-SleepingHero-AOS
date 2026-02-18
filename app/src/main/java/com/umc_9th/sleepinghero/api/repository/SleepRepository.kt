package com.umc_9th.sleepinghero.api.repository

/*
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsPageDto

class SleepRepository {
    private val sleepService = ApiClient.sleepService

    suspend fun getSleepSessions(token: String, page: Int, size: Int): ApiResponse<SleepSessionsPageDto> {
        return sleepService.getSleepSessions(token, page, size)
    }
}
*/

import com.umc_9th.sleepinghero.api.dto.GoalSleepRequest
import com.umc_9th.sleepinghero.api.dto.GoalSleepResult
import com.umc_9th.sleepinghero.api.dto.SleepEndResponse
import com.umc_9th.sleepinghero.api.dto.SleepRecordDetailResponse
import com.umc_9th.sleepinghero.api.dto.SleepReviewRequest
import com.umc_9th.sleepinghero.api.dto.SleepReviewResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsResponse
import com.umc_9th.sleepinghero.api.dto.SleepStartRequest
import com.umc_9th.sleepinghero.api.dto.SleepStartResponse
import com.umc_9th.sleepinghero.api.service.SleepService

class SleepRepository(private val sleepService: SleepService) {

    suspend fun getSleepRecordDetail(accessToken: String, sleepRecordId: Int): Result<SleepRecordDetailResponse> =
        try {
            val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.getSleepRecordDetail(token, sleepRecordId)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 기록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun startSleep(accessToken: String): Result<SleepStartResponse> =
        try {
            val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.startSleep(token)

            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 시작 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun createSleepReview(accessToken: String, recordId: Int, star: Int, comment: String): Result<SleepReviewResponse> =
        try {
            val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val request = SleepReviewRequest(recordId = recordId, star = star, comment = comment)
            val response = sleepService.createSleepReview(token, request)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "리뷰 작성 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun endSleep(accessToken: String): Result<SleepEndResponse> =
        try {
            val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.endSleep(token)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 종료 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getSleepSessions(accessToken: String, page: Int = 0, size: Int = 10): Result<SleepSessionsResponse> =
        try {
            val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.getSleepSessions(token, page, size)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 기록 목록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun putGoalSleep(accessToken: String, sleepTime: String, wakeTime: String): Result<GoalSleepResult> =
        runCatching {
            val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val res = sleepService.putGoalSleep(
                authorization = token,
                body = GoalSleepRequest(sleepTime = sleepTime, wakeTime = wakeTime)
            )
            if (!res.isSuccess) throw RuntimeException(res.message)
            res.result
        }
}


