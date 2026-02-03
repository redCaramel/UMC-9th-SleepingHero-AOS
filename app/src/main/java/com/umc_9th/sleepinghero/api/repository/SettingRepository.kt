package com.umc_9th.sleepinghero.api.repository

import android.util.Log
import com.umc_9th.sleepinghero.api.dto.FAQResponse
import com.umc_9th.sleepinghero.api.service.SettingService

class SettingRepository(private val service : SettingService) {

    suspend fun FAQUrl(accessToken : String
    ): Result<FAQResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val response = service.FAQUrl(token)
        if(response.isSuccess) {
            if(response.result == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            }
            else {
                Log.d("test", "FAQ Inquiry success.")
                Result.success(response.result)
            }
        }
        else {
            val errMsg = response.message
            Log.d("test", "error ${response.code} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code} : $errMsg"))
        }
    } catch(e : Exception) {
        Result.failure(e)
    }
}