package com.umc_9th.sleepinghero.api.repository

import com.umc_9th.sleepinghero.api.dto.TutorialStatusResponse
import com.umc_9th.sleepinghero.api.dto.TutorialUpdateRequest
import com.umc_9th.sleepinghero.api.dto.TutorialUpdateResponse
import com.umc_9th.sleepinghero.api.service.TutorialService

class TutorialRepository(private val service: TutorialService) {

    suspend fun getTutorial(accessToken: String): Result<TutorialStatusResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val res = service.getTutorial(token)
        if (res.isSuccessful) {
            val data = res.body()?.result ?: return Result.failure(RuntimeException("result null"))
            Result.success(data)
        } else {
            Result.failure(RuntimeException("HTTP ${res.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun finishTutorial(accessToken: String): Result<TutorialUpdateResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val res = service.patchTutorial(token, TutorialUpdateRequest(finished = true))
        if (res.isSuccessful) {
            val data = res.body()?.result ?: return Result.failure(RuntimeException("result null"))
            Result.success(data)
        } else {
            Result.failure(RuntimeException("HTTP ${res.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
