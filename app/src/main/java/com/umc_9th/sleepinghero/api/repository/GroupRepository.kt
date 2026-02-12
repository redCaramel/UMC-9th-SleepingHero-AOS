package com.umc_9th.sleepinghero.api.repository

import android.util.Log
import com.umc_9th.sleepinghero.api.ApiClient.retrofit
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.GroupCreateRequest
import com.umc_9th.sleepinghero.api.dto.GroupRankingInsideResponse
import com.umc_9th.sleepinghero.api.dto.GroupRankingResponse
import com.umc_9th.sleepinghero.api.service.GroupService

class GroupRepository(private val service : GroupService) {
    suspend fun CreateGroup(
        accessToken: String, req: GroupCreateRequest
    ): Result<String> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.CreateGroup(token, req)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Group Create Success.")
                Result.success(data)
            }
        } else {
            val errMsg = response.body()?.message.toString() ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()} : $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<String>

    suspend fun GroupCheck(
        accessToken: String
    ): Result<List<GroupRankingResponse>> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.CheckGroup(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Group Check Success.")
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
    } as Result<List<GroupRankingResponse>>

    suspend fun GroupRank(
        accessToken: String, req: String
    ): Result<GroupRankingInsideResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.GroupRanking(token, req)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Group Inside Check Success.")
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
    } as Result<GroupRankingInsideResponse>
}