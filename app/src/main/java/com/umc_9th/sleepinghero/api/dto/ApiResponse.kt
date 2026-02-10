package com.umc_9th.sleepinghero.api.dto

data class ApiResponse<T>(
    val isSuccess : Boolean,
    val code : String,
    val message : String,
    val result : T
)

