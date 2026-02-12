package com.umc_9th.sleepinghero.api.dto


data class DashBoardResponse(
    val heroId: Long,
    val currentStage: Int,
    val currentStreak: Int,
    val nonSleepStreak: Int
)