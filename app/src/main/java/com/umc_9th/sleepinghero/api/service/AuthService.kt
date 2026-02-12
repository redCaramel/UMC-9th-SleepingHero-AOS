package com.umc_9th.sleepinghero.api.service

import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.KakaoLoginRequest
import com.umc_9th.sleepinghero.api.dto.KakaoLoginResult
import com.umc_9th.sleepinghero.api.dto.NaverLoginRequest
import com.umc_9th.sleepinghero.api.dto.NaverLoginResult
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/auth/login/kakao")
    suspend fun loginKakao(
        @Body req: KakaoLoginRequest
    ) : ApiResponse<KakaoLoginResult>

    @POST("/auth/login/naver")
    suspend fun loginNaver(
        @Body req: NaverLoginRequest
    ) : ApiResponse<NaverLoginResult>

}
