package com.umc_9th.sleepinghero.api.dto

data class HeroDetailDTO(
    val heroId: Long,
    val name: String,
    val currentLevel: Int,
    val currentExp: Int,
    val needExp: Int,
    val currentStage: Int
)