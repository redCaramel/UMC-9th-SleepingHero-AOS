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
