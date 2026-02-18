package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.dto.SleepEndResponse
import com.umc_9th.sleepinghero.api.dto.SleepGoalRequest
import com.umc_9th.sleepinghero.api.dto.SleepGoalResponse
import com.umc_9th.sleepinghero.api.dto.SleepRecordDetailResponse
import com.umc_9th.sleepinghero.api.dto.SleepReviewRequest
import com.umc_9th.sleepinghero.api.dto.SleepReviewResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsResponse
import com.umc_9th.sleepinghero.api.dto.SleepStartResponse
import com.umc_9th.sleepinghero.api.service.SleepService

class SleepRepository(private val sleepService: SleepService) {

    private fun withBearer(token: String): String =
        if (token.startsWith("Bearer ")) token else "Bearer $token"

    /**
     * 9시간을 뺀 시각으로 변환하여 전달
     */
    private fun kstTimeToUtc(hhMmKst: String): String {
        val parts = hhMmKst.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        var totalMinutes = hour * 60 + minute - 9 * 60  // KST -> UTC (9시간 차감)
        if (totalMinutes < 0) totalMinutes += 24 * 60
        val utcHour = totalMinutes / 60
        val utcMinute = totalMinutes % 60
        return String.format("%02d:%02d", utcHour, utcMinute)
    }

    suspend fun getSleepRecordDetail(
        accessToken: String,
        sleepRecordId: Int
    ): Result<SleepRecordDetailResponse> =
        try {
            val token =
                if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.getSleepRecordDetail(token, sleepRecordId)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 기록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun startSleep(accessToken: String): Result<SleepStartResponse> =
        try {
            val token =
                if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.startSleep(token)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 시작 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun createSleepReview(
        accessToken: String,
        recordId: Int,
        star: Int,
        comment: String
    ): Result<SleepReviewResponse> =
        try {
            val token =
                if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val request = SleepReviewRequest(recordId = recordId, star = star, comment = comment)
            val response = sleepService.createSleepReview(token, request)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "리뷰 작성 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun endSleep(accessToken: String): Result<SleepEndResponse> =
        try {
            val token =
                if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.endSleep(token)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 종료 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getSleepSessions(
        accessToken: String,
        page: Int = 0,
        size: Int = 10
    ): Result<SleepSessionsResponse> =
        try {
            val token =
                if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
            val response = sleepService.getSleepSessions(token, page, size)
            if (response.isSuccess && response.result != null) Result.success(response.result)
            else Result.failure(Exception(response.message ?: "수면 기록 목록 조회 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun setSleepGoal(
        token: String,
        sleepTime: String,
        wakeTime: String
    ): Result<SleepGoalResponse> =
        try {
            val sleepTimeUtc = kstTimeToUtc(sleepTime)
            val wakeTimeUtc = kstTimeToUtc(wakeTime)

            val request = SleepGoalRequest(
                sleepTime = sleepTimeUtc,
                wakeTime = wakeTimeUtc
            )

            val response = sleepService.setSleepGoal(withBearer(token), request)

            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message ?: "목표 수면 시간 설정 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}


