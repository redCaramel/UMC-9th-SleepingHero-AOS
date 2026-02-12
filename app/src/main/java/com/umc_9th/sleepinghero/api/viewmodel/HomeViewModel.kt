package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.DashBoardResponse
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _homeDashboard = MutableLiveData<Result<DashBoardResponse>>()
    val homeDashboard: LiveData<Result<DashBoardResponse>> = _homeDashboard

    fun loadHomeDashboard(token: String) {
        viewModelScope.launch {
            val result = repository.getDashboard(token)
            _homeDashboard.postValue(result)
        }
    }
}