package com.umc_9th.sleepinghero.api.dto

data class GroupCreateRequest (
    val groupName : String,
    val description: String,
    val maxPeople : Long
)

data class GroupRankingResponse (
    val groupId : Long,
    val name : String,
    val maxPeople : Long,
    val currentPeople : Long,
    val rank : Long
)

data class GroupRankingInsideResponse (
    val groupName : String,
    val description: String,
    val totalMembers : Long,
    val totalGroupSleepTime : Long,
    val averageConsecutiveDays : Long,
    val memberRankings : MutableList<GroupMemberRanking>
)

data class GroupMemberRanking (
    val rank : Long,
    val memberName : String,
    val groupRole : String,
    val consecutiveSleepDays : Long,
    val totalSleepTime : Long,
    val level : Long
)