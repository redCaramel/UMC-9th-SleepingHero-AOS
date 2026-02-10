package com.umc_9th.sleepinghero

data class GroupData(
    val groupName : String,
    val description : String,
    val totalMembers : Long,
    val totalTime : Long,
    val streak : Long,
    val leader : String
)
