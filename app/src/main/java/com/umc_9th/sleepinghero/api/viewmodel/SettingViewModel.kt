package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.FAQRequest
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: SettingRepository) : ViewModel() {
    private val _faqUrlResult = MutableLiveData<Result<String>>()
    val faqUrlResult : LiveData<Result<String>> = _faqUrlResult

    fun FAQUrl(accessToken : String, type : String, content : String, responseEmail : String) {
        viewModelScope.launch {
            val request = FAQRequest(type, content, responseEmail)
            val result = repository.FAQUrl(accessToken, request)
            _faqUrlResult.postValue(result)
        }
    }
}
