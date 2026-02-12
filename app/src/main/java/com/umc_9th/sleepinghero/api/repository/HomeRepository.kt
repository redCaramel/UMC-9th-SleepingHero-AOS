package com.umc_9th.sleepinghero.api.repository

/*
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.dto.ApiResponse
import com.umc_9th.sleepinghero.api.dto.DashBoardResponse
import com.umc_9th.sleepinghero.api.dto.HeroDetailDTO

class HomeRepository {
    private val homeService = ApiClient.homeService

    suspend fun getDashboard(token: String): ApiResponse<DashBoardResponse> {
        return homeService.getDashboard(token)
    }
}
*/
import android.util.Log
import com.umc_9th.sleepinghero.api.dto.HomeDashboardResponse
import com.umc_9th.sleepinghero.api.service.HomeService

class HomeRepository(private val service: HomeService) {

    suspend fun getHomeDashboard(token: String): Result<HomeDashboardResponse> = try {
        val response = service.getHomeDashboard("Bearer $token")

        if (response.isSuccess) {
            if (response.result == null) {
                Log.d("HomeRepository", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                Log.d("HomeRepository", "Home Dashboard Success")
                Result.success(response.result)
            }
        } else {
            val errMsg = response.message
            Log.d("HomeRepository", "error ${response.code} : $errMsg")
            Result.failure(RuntimeException("HTTP ${response.code}: $errMsg"))
        }
    } catch (e: Exception) {
        Log.e("HomeRepository", "Exception: ${e.message}")
        Result.failure(e)
    }
}
