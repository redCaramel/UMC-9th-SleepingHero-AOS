package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.SleepEndResponse
import com.umc_9th.sleepinghero.api.dto.SleepRecordDetailResponse
import com.umc_9th.sleepinghero.api.dto.SleepReviewResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionsResponse
import com.umc_9th.sleepinghero.api.dto.SleepStartResponse
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import kotlinx.coroutines.launch

class SleepViewModel(private val repository: SleepRepository) : ViewModel() {

    // 수면 기록 상세
    private val _sleepRecordDetail = MutableLiveData<Result<SleepRecordDetailResponse>>()
    val sleepRecordDetail: LiveData<Result<SleepRecordDetailResponse>> = _sleepRecordDetail

    // 수면 시작
    private val _sleepStartResult = MutableLiveData<Result<SleepStartResponse>>()
    val sleepStartResult: LiveData<Result<SleepStartResponse>> = _sleepStartResult

    // 수면 리뷰
    private val _sleepReviewResult = MutableLiveData<Result<SleepReviewResponse>>()
    val sleepReviewResult: LiveData<Result<SleepReviewResponse>> = _sleepReviewResult

    // 수면 종료
    private val _sleepEndResult = MutableLiveData<Result<SleepEndResponse>>()
    val sleepEndResult: LiveData<Result<SleepEndResponse>> = _sleepEndResult

    // 수면 기록 목록
    private val _sleepSessions = MutableLiveData<Result<SleepSessionsResponse>>()
    val sleepSessions: LiveData<Result<SleepSessionsResponse>> = _sleepSessions

    /**
     * 수면 기록 상세 조회
     */
    fun loadSleepRecordDetail(token: String, sleepRecordId: Int) {
        viewModelScope.launch {
            val result = repository.getSleepRecordDetail(token, sleepRecordId)
            _sleepRecordDetail.value = result
        }
    }

    /**
     * 수면 시작 (목표와 동일한 sleepTime, wakeTime 전달)
     */
    fun startSleep(token: String, sleepTime: String, wakeTime: String) {
        viewModelScope.launch {
            val result = repository.startSleep(token, sleepTime, wakeTime)
            _sleepStartResult.value = result
        }
    }

    /**
     * 수면 리뷰 작성
     */
    fun createSleepReview(token: String, recordId: Int, star: Int, comment: String) {
        viewModelScope.launch {
            val result = repository.createSleepReview(token, recordId, star, comment)
            _sleepReviewResult.value = result
        }
    }

    /**
     * 수면 종료
     */
    fun endSleep(token: String) {
        viewModelScope.launch {
            val result = repository.endSleep(token)
            _sleepEndResult.value = result
        }
    }

    /**
     * 수면 기록 목록 조회
     */
    fun loadSleepSessions(token: String, page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            val result = repository.getSleepSessions(token, page, size)
            _sleepSessions.value = result
        }
    }
}