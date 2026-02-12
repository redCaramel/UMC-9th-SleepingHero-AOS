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
import com.umc_9th.sleepinghero.api.dto.DashBoardResponse
import com.umc_9th.sleepinghero.api.service.HomeService

class HomeRepository(private val service: HomeService) {

    private fun asBearer(token: String): String =
        if (token.startsWith("Bearer ")) token else "Bearer $token"

    suspend fun getDashboard(token: String): Result<DashBoardResponse> = try {
        val response = service.getDashboard(asBearer(token))

        if (response.isSuccess) {
            if (response.result == null) {
                Log.d("HomeRepository", "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            } else {
                Log.d("HomeRepository", "Dashboard Success")
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
