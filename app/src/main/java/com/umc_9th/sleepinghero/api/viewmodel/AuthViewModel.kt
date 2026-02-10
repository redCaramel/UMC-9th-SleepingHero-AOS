package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.KakaoLoginRequest
import com.umc_9th.sleepinghero.api.dto.KakaoLoginResult
import com.umc_9th.sleepinghero.api.dto.NaverLoginRequest
import com.umc_9th.sleepinghero.api.dto.NaverLoginResult
import com.umc_9th.sleepinghero.api.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _kakaoLoginResult = MutableLiveData<Result<KakaoLoginResult>>()
    val kakaoLoginResult : LiveData<Result<KakaoLoginResult>> = _kakaoLoginResult

    private val _naverLoginResult = MutableLiveData<Result<NaverLoginResult>>()
    val naverLoginResult : LiveData<Result<NaverLoginResult>> = _naverLoginResult

    fun kakaoLogin(accessToken : String) {
        viewModelScope.launch {
            val request = KakaoLoginRequest(accessToken)
            val result = repository.KakaoLogin(request)
            _kakaoLoginResult.postValue(result)
        }
    }

    fun naverLogin(accessToken: String) {
        viewModelScope.launch {
            val request = NaverLoginRequest(accessToken)
            val result = repository.NaverLogin(request)
            _naverLoginResult.postValue(result)
        }
    }
}
