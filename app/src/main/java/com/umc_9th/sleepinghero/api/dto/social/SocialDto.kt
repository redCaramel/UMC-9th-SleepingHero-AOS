package com.umc_9th.sleepinghero.api.dto.social

import com.google.gson.annotations.SerializedName

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

data class ChangeNameRequest (
    val name : String
)

data class ChangeNameResponse (
    val heroId : Int,
    val name : String,
    val currentLevel : Int,
    val currentExp : Int,
    val needExp : Int,
    val currentStage : Int
)

data class MyFriendResponse (
    val memberId : Long,
    val nickname : String,
    val profilePicture : String
)

data class FriendInviteRequest (
    @SerializedName("nickName")
    val nickName : String
)

data class RequestCheckResponse (
    val memberId : Int,
    val nickname : String,
    val profilePicture : String
)
data class SkinData (
    val skinId : Long,
    val name : String,
    val equipped : Boolean
)
data class CheckSkinResponse (
    val skins : List<SkinData>
)

data class DeleteFriendRequest (
    val nickName : String
)