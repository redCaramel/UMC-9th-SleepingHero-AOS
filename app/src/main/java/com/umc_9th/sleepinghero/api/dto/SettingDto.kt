package com.umc_9th.sleepinghero.api.dto

data class FAQRequest(
    val type : String,
    val content : String,
    val responseEmail : String
)