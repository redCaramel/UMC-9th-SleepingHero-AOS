package com.umc_9th.sleepinghero.api.dto


data class SkinInfoDTO(
    val skinId: Long,
    val name: String,
    val equipped: Boolean
)

data class SkinListDTO(
    val skins: List<SkinInfoDTO>
)