package com.umc_9th.sleepinghero

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.umc_9th.sleepinghero.BuildConfig
class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}