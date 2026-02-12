package com.umc_9th.sleepinghero

import com.umc_9th.sleepinghero.api.dto.GroupMemberRanking

data class GroupData(
    val groupName : String,
    val description : String,
    val totalMembers : Long,
    val totalTime : Long,
    val streak : Long,
    val leader : String,
    val icon : Long,
    val members : MutableList<GroupMemberRanking>
)
