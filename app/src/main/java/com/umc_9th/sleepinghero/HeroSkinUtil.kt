package com.umc_9th.sleepinghero

enum class HeroCondition { BEST, LOW, BAD, COLD }

object HeroSkinUtil {

    fun calculateCondition(nonSleepStreak: Int): HeroCondition {
        return when {
            nonSleepStreak <= 0 -> HeroCondition.BEST
            nonSleepStreak == 1 -> HeroCondition.LOW
            nonSleepStreak == 2 -> HeroCondition.BAD
            else -> HeroCondition.COLD
        }
    }

    fun resolveSkin(baseSkinId: Long, condition: HeroCondition): Long {
        val groupStart = ((baseSkinId - 1) / 3) * 3 + 1
        return when (condition) {
            HeroCondition.BEST -> groupStart + 1
            HeroCondition.LOW  -> groupStart
            HeroCondition.BAD  -> groupStart
            HeroCondition.COLD -> groupStart + 2
        }
    }
}