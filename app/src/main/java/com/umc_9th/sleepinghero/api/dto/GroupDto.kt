package com.umc_9th.sleepinghero.api.dto

data class GroupCreateRequest (
    val groupName : String,
    val description: String,
    val maxPeople : Long,
    val groupImageId: Int
)

data class GroupRankingResponse (
    val groupId : Long,
    val name : String,
    val maxPeople : Long,
    val currentPeople : Long,
    val rank : Long,
    val groupImageId : Int
)

data class GroupRankingInsideResponse (
    val groupName : String,
    val description: String,
    val totalMembers : Long,
    val totalGroupSleepTime : Long,
    val averageConsecutiveDays : Long,
    val memberRankings : MutableList<GroupMemberRanking>,
    val groupImageId : Int,
    val groupMasterNickname : String
)

data class GroupMemberRanking (
    val rank : Long,
    val memberName : String,
    val groupRole : String,
    val consecutiveSleepDays : Long,
    val totalSleepTime : Long,
    val level : Long
)

data class GroupRankingRequest (
    val groupName : String
)

data class GroupInviteRequest (
    val groupName : String,
    val nickName : String
)

data class GroupRequestCheckResponse(
    val groupName : String
)
data class GroupRequestRequest(
    val groupName : String
)