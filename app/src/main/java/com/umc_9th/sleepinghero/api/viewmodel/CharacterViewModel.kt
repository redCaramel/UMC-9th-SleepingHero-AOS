package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.CharacterInfoResponse
import com.umc_9th.sleepinghero.api.repository.CharacterRepository
import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: CharacterRepository) : ViewModel() {

    // 캐릭터 정보
    private val _characterInfo = MutableLiveData<Result<CharacterInfoResponse>>()
    val characterInfo: LiveData<Result<CharacterInfoResponse>> = _characterInfo

    /**
     * 캐릭터 정보 조회
     * GET /characters/me
     */
    fun loadCharacterInfo(token: String) {
        viewModelScope.launch {
            val result = repository.getCharacterInfo(token)
            _characterInfo.value = result
        }
    }
}