package com.umc_9th.sleepinghero.api.repository

import android.util.Log
import com.umc_9th.sleepinghero.api.ApiClient.retrofit
import com.umc_9th.sleepinghero.api.dto.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.FAQResponse
import com.umc_9th.sleepinghero.api.dto.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.MyCharResponse
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.ChangeNameRequest
import com.umc_9th.sleepinghero.api.dto.ChangeNameResponse
import com.umc_9th.sleepinghero.api.dto.FriendInviteRequest
import com.umc_9th.sleepinghero.api.dto.MyFriendResponse
import com.umc_9th.sleepinghero.api.dto.RequestCheckResponse
import com.umc_9th.sleepinghero.api.service.SocialService

class SocialRepository(private val service : SocialService) {
    suspend fun CharacterSearch(
        accessToken: String, req: CharSearchRequest
    ): Result<CharSearchResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.CharacterSearch(token, req.name)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Hero Search success.")
                Result.success(data)
            }
        } else {
            val errMsg = response.body()?.message.toString() ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()} : $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<CharSearchResponse>

    suspend fun MyCharacterCheck(
        accessToken: String
    ): Result<MyCharResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.MyCharacter(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Hero Search success.")
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
    } as Result<MyCharResponse>

    suspend fun LoadFriendRank(
        accessToken: String
    ): Result<List<FriendRankingResponse>> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.FriendRanking(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Rank load success.")
                Result.success(data)
            }
        } else {
            val errMsg = response.body()?.message.toString() ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()} : $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<List<FriendRankingResponse>>

    suspend fun ChangeName(
        accessToken: String, req: ChangeNameRequest
    ): Result<ChangeNameResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.ChangeName(token, req)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Nickname Change success.")
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
    } as Result<ChangeNameResponse>

    suspend fun MyFriends(
        accessToken: String
    ): Result<List<MyFriendResponse>> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.MyFriends(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Friend load success.")
                Result.success(data)
            }
        } else {
            val adapter = retrofit.responseBodyConverter<ApiResponse<Any>>(
                ApiResponse::class.java,
                arrayOfNulls<Annotation>(0)
            )
            val errMsg = adapter.convert(response.errorBody())?.message ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<List<MyFriendResponse>>

    suspend fun FriendInvite(
        accessToken: String, req: FriendInviteRequest
    ): Result<String> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.FriendInvite(token, req)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Friend Invite success.")
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
    } as Result<String>

    suspend fun CheckRequest(
        accessToken: String
    ): Result<List<RequestCheckResponse>> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.CheckRequest(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Request Check success.")
                Result.success(data)
            }
        } else {
            val errMsg = response.body()?.message.toString() ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()} : $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<List<RequestCheckResponse>>
}
