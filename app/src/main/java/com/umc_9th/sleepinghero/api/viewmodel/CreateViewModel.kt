package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.CreateHeroResponse
import com.umc_9th.sleepinghero.api.repository.CreateRepository
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import kotlinx.coroutines.launch

class CreateViewModel(private val repository: CreateRepository) : ViewModel() {
    private val _heroCreateResult = MutableLiveData<Result<CreateHeroResponse>>()
    val heroCreateResult : LiveData<Result<CreateHeroResponse>> = _heroCreateResult

    fun CreateHero(accessToken : String) {
        viewModelScope.launch {
            val result = repository.CreateHero(accessToken)
            _heroCreateResult.postValue(result)
        }
    }
}