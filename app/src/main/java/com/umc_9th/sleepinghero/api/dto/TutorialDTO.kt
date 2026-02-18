package com.umc_9th.sleepinghero.api.dto

data class TutorialStatusResponse(
    val finished: Boolean
)

data class TutorialUpdateRequest(
    val finished: Boolean
)

data class TutorialUpdateResponse(
    val memberId: Long,
    val finished: Boolean
)