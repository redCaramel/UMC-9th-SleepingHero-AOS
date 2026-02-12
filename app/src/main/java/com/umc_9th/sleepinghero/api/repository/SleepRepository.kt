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

import com.umc_9th.sleepinghero.api.dto.SleepEndResponse
import com.umc_9th.sleepinghero.api.dto.SleepRecordDetailResponse
import com.umc_9th.sleepinghero.api.dto.SleepReviewRequest
import com.umc_9th.sleepinghero.api.dto.SleepReviewResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsResponse
import com.umc_9th.sleepinghero.api.dto.SleepStartResponse
import com.umc_9th.sleepinghero.api.service.SleepService

class SleepRepository(private val sleepService: SleepService) {

    private fun withBearer(token: String): String =
        if (token.startsWith("Bearer ")) token else "Bearer $token"

    suspend fun getSleepRecordDetail(token: String, sleepRecordId: Int): Result<SleepRecordDetailResponse> =
        try {
            val response = sleepService.getSleepRecordDetail(withBearer(token), sleepRecordId)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 기록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun startSleep(token: String): Result<SleepStartResponse> =
        try {
            val response = sleepService.startSleep(withBearer(token))
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 시작 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun createSleepReview(token: String, recordId: Int, star: Int, comment: String): Result<SleepReviewResponse> =
        try {
            val request = SleepReviewRequest(recordId = recordId, star = star, comment = comment)
            val response = sleepService.createSleepReview(withBearer(token), request)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "리뷰 작성 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun endSleep(token: String): Result<SleepEndResponse> =
        try {
            val response = sleepService.endSleep(withBearer(token))
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 종료 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getSleepSessions(token: String, page: Int = 0, size: Int = 10): Result<SleepSessionsResponse> =
        try {
            val response = sleepService.getSleepSessions(withBearer(token), page, size)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 기록 목록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
}

