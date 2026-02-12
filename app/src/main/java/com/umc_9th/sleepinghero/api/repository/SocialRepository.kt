package com.umc_9th.sleepinghero.api.repository

import android.util.Log
import com.umc_9th.sleepinghero.api.ApiClient.retrofit
import com.umc_9th.sleepinghero.api.dto.social.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.social.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.social.MyCharResponse
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.social.ChangeNameRequest
import com.umc_9th.sleepinghero.api.dto.social.ChangeNameResponse
import com.umc_9th.sleepinghero.api.dto.social.CheckSkinResponse
import com.umc_9th.sleepinghero.api.dto.social.DeleteFriendRequest
import com.umc_9th.sleepinghero.api.dto.social.FriendInviteRequest
import com.umc_9th.sleepinghero.api.dto.social.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.social.FriendRequestStatusUpdate
import com.umc_9th.sleepinghero.api.dto.social.MyFriendResponse
import com.umc_9th.sleepinghero.api.dto.social.RequestCheckResponse
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
                if(data?.isEmpty() ?: false) Log.d("test", "noData")
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

    suspend fun CheckSkin(
        accessToken: String
    ): Result<CheckSkinResponse> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.CheckSkin(token)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Skin Check success.")
                Result.success(data)
            }
        } else {
            val errMsg = response.body()?.message.toString() ?: "Unknown error"
            Log.d("test", "error ${response.code()} : $errMsg")
            Result.failure(java.lang.RuntimeException("HTTP ${response.code()} : $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    } as Result<CheckSkinResponse>

    suspend fun EquipSkin(
        accessToken: String, skinId : Int
    ): Result<String> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val response = service.EquipSKin(token, skinId)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Skin Equip success.")
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

    suspend fun ResponseRequest(
        accessToken: String,
        status: String,
        nickName: String
    ): Result<String> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val requestBody = FriendRequestStatusUpdate(nickName)
        val response = service.updateFriendRequest(token, status, requestBody)

        if (response.isSuccessful) {
            val body = response.body()
            if (body?.result == null) {
                Log.d("test", "Response result is null")
                Result.success("Success (No result data)")
            } else {
                Log.d("test", "Friend Response success: ${body.result}")
                Result.success(body.result)
            }
        } else {
            val errMsg = response.errorBody()?.string() ?: "Unknown error"
            Log.e("test", "Error ${response.code()}: $errMsg")
            Result.failure(RuntimeException("HTTP ${response.code()}: $errMsg"))
        }
    } catch (e: Exception) {
        Log.e("test", "Exception: ${e.message}")
        Result.failure(e)
    }

    suspend fun DeleteFriend(
        accessToken: String, nickName : String
    ): Result<String> = try {
        val token = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        Log.d("test", token)
        val req = DeleteFriendRequest(nickName)
        val response = service.DeleteFriend(token, req)
        if (response.isSuccessful) {
            if (response.body() == null) {
                Log.d("test", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                val data = response.body()?.result
                Log.d("test", "Friend Delete success.")
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

}
