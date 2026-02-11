package com.umc_9th.sleepinghero.api

import com.umc_9th.sleepinghero.api.service.AuthService
import com.umc_9th.sleepinghero.api.service.CreateService
import com.umc_9th.sleepinghero.api.service.GroupService
import com.umc_9th.sleepinghero.api.service.SettingService
import com.umc_9th.sleepinghero.api.service.SocialService
import com.umc_9th.sleepinghero.api.service.CharacterService
import com.umc_9th.sleepinghero.api.service.FriendService
import com.umc_9th.sleepinghero.api.service.HomeService
import com.umc_9th.sleepinghero.api.service.SleepService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://3.37.240.159:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    val authService : AuthService = retrofit.create(AuthService::class.java)
    val settingService: SettingService = retrofit.create(SettingService::class.java)
    val socialService: SocialService = retrofit.create(SocialService::class.java)
    val createService : CreateService = retrofit.create(CreateService::class.java)
    val groupService : GroupService = retrofit.create(GroupService::class.java)
    val homeService: HomeService = retrofit.create(HomeService::class.java)
    val characterService: CharacterService = retrofit.create(CharacterService::class.java)
    val sleepService: SleepService = retrofit.create(SleepService::class.java)
    val friendService: FriendService = retrofit.create(FriendService::class.java)
}
