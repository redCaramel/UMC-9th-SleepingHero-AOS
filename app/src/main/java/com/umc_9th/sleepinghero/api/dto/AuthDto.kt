package com.umc_9th.sleepinghero.api.dto

data class KakaoLoginRequest(
    val accessToken : String
)

data class KakaoLoginResult(
    val memberId : Int,
    val nickName : String,
    val accessToken: String
)

data class NaverLoginRequest(
    val accessToken : String
)

data class NaverLoginResult(
    val memberId : Int,
    val nickName : String,
    val accessToken: String
)
