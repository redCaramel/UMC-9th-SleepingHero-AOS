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

    /**
     * 수면 기록 상세 조회
     * GET /sleep-sessions/{sleepRecordId}
     */
    suspend fun getSleepRecordDetail(
        token: String,
        sleepRecordId: Int
    ): Result<SleepRecordDetailResponse> {
        return try {
            val response = sleepService.getSleepRecordDetail("Bearer $token", sleepRecordId)

            if (response.isSuccess) {
                Result.success(response.result!!)
            } else {
                Result.failure(Exception(response.message ?: "수면 기록 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 수면 시작
     * POST /sleep-sessions/start
     */
    suspend fun startSleep(token: String): Result<SleepStartResponse> {
        return try {
            val response = sleepService.startSleep("Bearer $token")

            if (response.isSuccess) {
                Result.success(response.result!!)
            } else {
                Result.failure(Exception(response.message ?: "수면 시작 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 수면 리뷰 작성
     * POST /sleep-sessions/review
     */
    suspend fun createSleepReview(
        token: String,
        recordId: Int,
        star: Int,
        comment: String
    ): Result<SleepReviewResponse> {
        return try {
            val request = SleepReviewRequest(
                recordId = recordId,
                star = star,
                comment = comment
            )
            val response = sleepService.createSleepReview("Bearer $token", request)

            if (response.isSuccess) {
                Result.success(response.result!!)
            } else {
                Result.failure(Exception(response.message ?: "리뷰 작성 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 수면 종료
     * POST /sleep-sessions/end
     */
    suspend fun endSleep(token: String): Result<SleepEndResponse> {
        return try {
            val response = sleepService.endSleep("Bearer $token")

            if (response.isSuccess) {
                Result.success(response.result!!)
            } else {
                Result.failure(Exception(response.message ?: "수면 종료 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 수면 기록 목록 조회
     * GET /sleep-sessions
     */
    suspend fun getSleepSessions(
        token: String,
        page: Int = 0,
        size: Int = 10
    ): Result<SleepSessionsResponse> {
        return try {
            val response = sleepService.getSleepSessions("Bearer $token", page, size)

            if (response.isSuccess) {
                Result.success(response.result!!)
            } else {
                Result.failure(Exception(response.message ?: "수면 기록 목록 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
