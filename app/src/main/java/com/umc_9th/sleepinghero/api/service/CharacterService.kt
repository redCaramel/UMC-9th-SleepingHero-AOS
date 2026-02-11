package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.CharacterInfoResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface CharacterService {
    /**
     * 캐릭터 상세 조회
     * GET /characters/me
     *
     * 응답 예시:
     * {
     *   "heroId": 0,
     *   "name": "string",
     *   "currentLevel": 0,
     *   "currentExp": 0,
     *   "needExp": 0,
     *   "currentStage": 0
     * }
     */
    @GET("/characters/me")
    suspend fun getCharacterInfo(
        @Header("Authorization") token: String
    ): ApiResponse<CharacterInfoResponse>
}