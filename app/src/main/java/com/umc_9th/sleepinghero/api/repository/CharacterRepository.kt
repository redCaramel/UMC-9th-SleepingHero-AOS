package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.dto.CharacterInfoResponse
import com.umc_9th.sleepinghero.api.service.CharacterService

class CharacterRepository(private val characterService: CharacterService) {

    /**
     * 캐릭터 정보 조회
     * GET /characters/me
     */
    suspend fun getCharacterInfo(token: String): Result<CharacterInfoResponse> {
        return try {
            val response = characterService.getCharacterInfo("Bearer $token")

            if (response.isSuccess) {
                Result.success(response.result!!)
            } else {
                Result.failure(Exception(response.message ?: "캐릭터 정보 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}