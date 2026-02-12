package com.umc_9th.sleepinghero.api.dto

data class SleepSessionsPageDto(
    val content: List<SleepSessionDto>
)

data class SleepSessionDto(
    val recordId: Long,
    val sleptTime: String,
    val wokeTime: String,
    val isSuccess: Boolean
)

// TODO: 위에 Ber 밑에 Theo 중복 있어서 조정 필요

/**
 * GET /sleep-sessions/{sleepRecordId} - 수면 기록 상세 조회
 */
data class SleepRecordDetailResponse(
    val recordId: Int,
    val sleptTime: String,      // "2026-02-11T10:13:42.157Z"
    val wokeTime: String,        // "2026-02-11T10:13:42.157Z"
    val isSuccess: Boolean
)

/**
 * POST /sleep-sessions/start - 수면 시작
 */
data class SleepStartResponse(
    val recordId: Int,
    val sleepTime: String,       // "2026-02-11T10:14:01.863Z"
    val sleepStatus: Boolean
)

/**
 * POST /sleep-sessions/review - 수면 리뷰 작성
 */
data class SleepReviewRequest(
    val recordId: Int,
    val star: Int,
    val comment: String
)

data class SleepReviewResponse(
    val reviewId: Int,
    val summary: String,
    val positives: List<String>,
    val improvements: List<String>,
    val cheering: String
)

/**
 * POST /sleep-sessions/end - 수면 종료
 */
data class SleepEndResponse(
    val recordId: Int,
    val sleptTime: String,           // "2026-02-11T10:15:03.804Z"
    val wokeTime: String,             // "2026-02-11T10:15:03.804Z"
    val durationMinutes: Int,
    val sleepReward: SleepReward,
    val currentStage: Int
)

data class SleepReward(
    val gainedExp: Int,
    val isDebuff: Boolean,
    val levelChange: LevelChange?
)

data class LevelChange(
    val prevLevel: Int,
    val currentLevel: Int,
    val currentExp: Int,
    val needExp: Int
)

/**
 * GET /sleep-sessions - 수면 기록 목록 조회 (페이징)
 */
data class SleepSessionsResponse(
    val totalElements: Int,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val size: Int,
    val content: List<SleepSessionItem>,
    val number: Int,
    val numberOfElements: Int,
    val empty: Boolean
)

data class SleepSessionItem(
    val recordId: Int,
    val sleptTime: String,
    val wokeTime: String,
    val isSuccess: Boolean
)
