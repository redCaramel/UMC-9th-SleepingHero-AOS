package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.GroupCreateRequest
import com.umc_9th.sleepinghero.api.dto.GroupRankingInsideResponse
import com.umc_9th.sleepinghero.api.dto.GroupRankingResponse
import com.umc_9th.sleepinghero.api.repository.GroupRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import kotlinx.coroutines.launch

class GroupViewModel(private val repository: GroupRepository) : ViewModel() {
    private val _createGroupResponse = MutableLiveData<Result<String>>()
    val createGroupResponse : LiveData<Result<String>> = _createGroupResponse

    private val _groupCheckResponse = MutableLiveData<Result<List<GroupRankingResponse>>>()
    val groupCheckResponse : LiveData<Result<List<GroupRankingResponse>>> = _groupCheckResponse
    private val _groupRankResponse = MutableLiveData<Result<GroupRankingInsideResponse>>()
    val groupRankResponse : LiveData<Result<GroupRankingInsideResponse>> = _groupRankResponse

    fun createGroup(accessToken : String, name : String, info : String, max : Long, icon : Int) {
        viewModelScope.launch {
            val request = GroupCreateRequest(name, info, max, icon)
            val result = repository.CreateGroup(accessToken, request)
            _createGroupResponse.postValue(result)
        }
    }
    fun groupCheck(accessToken: String) {
        viewModelScope.launch {
            val result = repository.GroupCheck(accessToken)
            _groupCheckResponse.postValue(result)
        }
    }
    fun groupRank(accessToken: String, name:String) {
        viewModelScope.launch {
            val result = repository.GroupRank(accessToken, name)
            _groupRankResponse.postValue(result)
        }
    }
}