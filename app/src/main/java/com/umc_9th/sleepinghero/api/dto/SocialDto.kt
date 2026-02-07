package com.umc_9th.sleepinghero.api.dto

data class CharSearchRequest (
    val name : String
)

data class CharSearchResponse (
    val memberId : Int,
    val heroId : Int,
    val heroName : String,
    val level : Int,
    val skinId : Int,
    val continuousSleepDays : Int,
    val totalSleepHour : Int
)

data class MyCharResponse(
    val heroId : Int,
    val name : String,
    val currentLevel : Int,
    val currentExp : Int,
    val needExp : Int,
    val currentStage : Int
)

data class FriendRankingResponse (
    val nickName : String,
    val totalSleepTime : String,
    val rank : Int
)