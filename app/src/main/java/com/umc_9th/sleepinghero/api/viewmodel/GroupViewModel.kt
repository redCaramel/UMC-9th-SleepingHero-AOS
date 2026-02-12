package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.GroupCreateRequest
import com.umc_9th.sleepinghero.api.dto.GroupInviteRequest
import com.umc_9th.sleepinghero.api.dto.GroupRankingInsideResponse
import com.umc_9th.sleepinghero.api.dto.GroupRankingResponse
import com.umc_9th.sleepinghero.api.dto.GroupRequestCheckResponse
import com.umc_9th.sleepinghero.api.dto.GroupRequestRequest
import com.umc_9th.sleepinghero.api.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupViewModel(private val repository: GroupRepository) : ViewModel() {
    private val _createGroupResponse = MutableLiveData<Result<String>>()
    val createGroupResponse : LiveData<Result<String>> = _createGroupResponse

    private val _groupCheckResponse = MutableLiveData<Result<List<GroupRankingResponse>>>()
    val groupCheckResponse : LiveData<Result<List<GroupRankingResponse>>> = _groupCheckResponse
    private val _groupRankResponse = MutableLiveData<Result<GroupRankingInsideResponse>>()
    val groupRankResponse : LiveData<Result<GroupRankingInsideResponse>> = _groupRankResponse
    private val _groupInviteResponse = MutableLiveData<Result<String>>()
    val groupInviteResponse : LiveData<Result<String>> = _groupInviteResponse
    private val _groupRequestCheckResponse = MutableLiveData<Result<List< GroupRequestCheckResponse>>>()
    val groupRequestCheckResponse : LiveData<Result<List<GroupRequestCheckResponse>>> = _groupRequestCheckResponse
    private val _groupRequestResponse = MutableLiveData<Result<String>>()
    val groupRequestResponse : LiveData<Result<String>> = _groupRequestResponse
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
    fun groupInvite(accessToken: String, groupName : String, nickName : String) {
        viewModelScope.launch {
            val request = GroupInviteRequest(groupName, nickName)
            val result = repository.GroupInvite(accessToken, request)
            _groupInviteResponse.postValue(result)
        }
    }
    fun groupRequestCheck(accessToken: String) {
        viewModelScope.launch {
            val result = repository.GroupRequestCheck(accessToken)
            _groupRequestCheckResponse.postValue(result)
        }
    }
    fun groupRequest(accessToken: String, status : String, groupName : String) {
        viewModelScope.launch {
            val request = GroupRequestRequest(groupName)
            val result = repository.GroupRequest(accessToken, status, request)
            _groupRequestResponse.postValue(result)
        }
    }

}