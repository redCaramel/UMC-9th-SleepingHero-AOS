package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.FAQResponse
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: SettingRepository) : ViewModel() {
    private val _faqUrlResult = MutableLiveData<Result<FAQResponse>>()
    val faqUrlResult : LiveData<Result<FAQResponse>> = _faqUrlResult

    fun FAQUrl(accessToken : String) {
        viewModelScope.launch {
            val result = repository.FAQUrl(accessToken)
            _faqUrlResult.postValue(result)
        }
    }
}
