package com.umc_9th.sleepinghero.api

import com.umc_9th.sleepinghero.api.service.AuthService
import com.umc_9th.sleepinghero.api.service.HeroService
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
    val heroService: HeroService = retrofit.create(HeroService::class.java)
    val homeService: HomeService = retrofit.create(HomeService::class.java)
    val sleepService: SleepService = retrofit.create(SleepService::class.java)

}

