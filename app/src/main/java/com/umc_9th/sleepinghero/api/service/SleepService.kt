package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse

import com.umc_9th.sleepinghero.api.dto.SleepEndResponse
import com.umc_9th.sleepinghero.api.dto.SleepGoalRequest
import com.umc_9th.sleepinghero.api.dto.SleepGoalResponse
import com.umc_9th.sleepinghero.api.dto.SleepRecordDetailResponse
import com.umc_9th.sleepinghero.api.dto.SleepReviewRequest
import com.umc_9th.sleepinghero.api.dto.SleepReviewResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsResponse
import com.umc_9th.sleepinghero.api.dto.SleepStartResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SleepService {

    /**
     * 수면 기록 상세 조회
     * GET /sleep-sessions/{sleepRecordId}
     */
    @GET("/sleep-sessions/{sleepRecordId}")
    suspend fun getSleepRecordDetail(
        @Header("Authorization") token: String,
        @Path("sleepRecordId") sleepRecordId: Int
    ): ApiResponse<SleepRecordDetailResponse>

    /**
     * 수면 시작
     * POST /sleep-sessions/start
     * 파라미터 없음
     */
    @POST("/sleep-sessions/start")
    suspend fun startSleep(
        @Header("Authorization") token: String
    ): ApiResponse<SleepStartResponse>

    /**
     * 수면 리뷰 작성
     * POST /sleep-sessions/review
     *
     * Request Body:
     * {
     *   "recordId": 0,
     *   "star": 5,
     *   "comment": "string"
     * }
     */
    @POST("/sleep-sessions/review")
    suspend fun createSleepReview(
        @Header("Authorization") token: String,
        @Body request: SleepReviewRequest
    ): ApiResponse<SleepReviewResponse>

    /**
     * 수면 종료
     * POST /sleep-sessions/end
     * 파라미터 없음
     */
    @POST("/sleep-sessions/end")
    suspend fun endSleep(
        @Header("Authorization") token: String
    ): ApiResponse<SleepEndResponse>

    /**
     * 수면 기록 목록 조회 (페이징)
     * GET /sleep-sessions
     *
     * Query Parameters:
     * - page: 페이지 번호 (기본값 0)
     * - size: 페이지 크기 (기본값 10)
     */
    @GET("/sleep-sessions")
    suspend fun getSleepSessions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<SleepSessionsResponse>

    /**
     * 목표 수면 시간 설정
     * PUT /sleep-sessions/goal
     *
     * Request Body:
     * {
     *   "sleepTime": "23:00",
     *   "wakeTime": "07:00"
     * }
     */
    @PUT("/sleep-sessions/goal")
    suspend fun setSleepGoal(
        @Header("Authorization") token: String,
        @Body request: SleepGoalRequest
    ): ApiResponse<SleepGoalResponse>
}