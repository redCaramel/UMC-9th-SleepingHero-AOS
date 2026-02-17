package com.umc_9th.sleepinghero.api.repository

import android.util.Log
import com.umc_9th.sleepinghero.api.ApiClient.retrofit
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.CreateHeroResponse
import com.umc_9th.sleepinghero.api.service.CreateService

class CreateRepository(private val service : CreateService) {
    suspend fun CreateHero(
        accessToken: String
    ): Result<CreateHeroResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val response = service.CreateHero(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Hero Create Success.")
                Result.success(data)
            }
        } else {
            val adapter = retrofit.responseBodyConverter<ApiResponse<Any>>(
            ApiResponse::class.java,
            arrayOfNulls<Annotation>(0)
            )
            val errMsg = adapter.convert(response.errorBody())?.message ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()} : $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<CreateHeroResponse>
}