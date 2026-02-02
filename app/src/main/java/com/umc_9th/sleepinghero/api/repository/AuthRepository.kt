package com.umc_9th.sleepinghero.api.repository

import android.util.Log
import com.umc_9th.sleepinghero.api.dto.KakaoLoginRequest
import com.umc_9th.sleepinghero.api.dto.KakaoLoginResult
import com.umc_9th.sleepinghero.api.dto.NaverLoginRequest
import com.umc_9th.sleepinghero.api.dto.NaverLoginResult
import com.umc_9th.sleepinghero.api.service.AuthService

class AuthRepository(private val service: AuthService) {

    suspend fun KakaoLogin(req: KakaoLoginRequest
    ): Result<KakaoLoginResult> = try {
        val response = service.loginKakao(req)

        if (response.isSuccess) {
            if(response.result == null){
                Log.d("tag", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            }
            else{
                Log.d("tag", "Kakao Login Success")
                Result.success(response.result)
            }
        }
        else{
            val errMsg = response.message
            Log.d("tag", "error ${response.code} : $errMsg")
            Result.failure(RuntimeException("HTTP ${response.code}: $errMsg"))
        }
    } catch (e: Exception){
        Result.failure(e)
    }

    suspend fun NaverLogin(req: NaverLoginRequest
    ): Result<NaverLoginResult> = try {
        val response = service.loginNaver(req)

        if (response.isSuccess) {
            if(response.result == null){
                Log.d("tag", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            }
            else{
                Log.d("tag", "Naver Login Success")
                Result.success(response.result)
            }
        }
        else{
            val errMsg = response.message
            Log.d("tag", "error ${response.code} : $errMsg")
            Result.failure(RuntimeException("HTTP ${response.code}: $errMsg"))
        }
    } catch (e: Exception){
        Result.failure(e)
    }
}