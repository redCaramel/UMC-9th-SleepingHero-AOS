package com.umc_9th.sleepinghero.api.dto

// GET /home/dashboard 응답
data class HomeDashboardResponse(
    val heroId: Int,              // 캐릭터 ID
    val currentStage: Int,        // 현재 스테이지 레벨, 영웅 레벨
    val currentStreak: Int,       // 연속 수면일
    val nonSleepStreak: Int       // 연속 미수면일
)