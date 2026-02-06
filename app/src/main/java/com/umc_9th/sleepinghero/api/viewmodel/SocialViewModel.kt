package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.FAQResponse
import com.umc_9th.sleepinghero.api.dto.MyCharResponse
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import kotlinx.coroutines.launch

class SocialViewModel(private val repository: SocialRepository) : ViewModel()  {
    private val _charSearchResponse = MutableLiveData<Result<CharSearchResponse>>()
    val charSearchResponse : LiveData<Result<CharSearchResponse>> = _charSearchResponse

    private val _myCharResponse = MutableLiveData<Result<MyCharResponse>>()
    val myCharResponse : LiveData<Result<MyCharResponse>> = _myCharResponse

    fun charSearch(accessToken : String, name : String) {
        viewModelScope.launch {
            val request = CharSearchRequest(name)
            val result = repository.CharacterSearch(accessToken, request)
            _charSearchResponse.postValue(result)
        }
    }

    fun myCharacter(accessToken: String) {
        viewModelScope.launch {
            val result = repository.MyCharacterCheck(accessToken)
            _myCharResponse.postValue(result)
        }
    }


}