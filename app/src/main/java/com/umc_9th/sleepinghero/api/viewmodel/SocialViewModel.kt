package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.ChangeNameRequest
import com.umc_9th.sleepinghero.api.dto.ChangeNameResponse
import com.umc_9th.sleepinghero.api.dto.CharSearchRequest
import com.umc_9th.sleepinghero.api.dto.CharSearchResponse
import com.umc_9th.sleepinghero.api.dto.FAQResponse
import com.umc_9th.sleepinghero.api.dto.FriendInviteRequest
import com.umc_9th.sleepinghero.api.dto.FriendRankingResponse
import com.umc_9th.sleepinghero.api.dto.MyCharResponse
import com.umc_9th.sleepinghero.api.dto.MyFriendResponse
import com.umc_9th.sleepinghero.api.dto.RequestCheckResponse
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import kotlinx.coroutines.launch

class SocialViewModel(private val repository: SocialRepository) : ViewModel()  {
    private val _charSearchResponse = MutableLiveData<Result<CharSearchResponse>>()
    val charSearchResponse : LiveData<Result<CharSearchResponse>> = _charSearchResponse

    private val _myCharResponse = MutableLiveData<Result<MyCharResponse>>()
    val myCharResponse : LiveData<Result<MyCharResponse>> = _myCharResponse

    private val _friendRankingResponse = MutableLiveData<Result<List<FriendRankingResponse>>>()
    val friendRankingResponse : LiveData<Result<List<FriendRankingResponse>>> = _friendRankingResponse

    private val _changeNameResponse = MutableLiveData<Result<ChangeNameResponse>>()
    val changeNameResponse : LiveData<Result<ChangeNameResponse>> = _changeNameResponse

    private val _myFriendResponse = MutableLiveData<Result<List<MyFriendResponse>>>()
    val myFriendResponse : LiveData<Result<List<MyFriendResponse>>> = _myFriendResponse

    private val _friendInviteResponse = MutableLiveData<Result<String>>()
    val friendInviteResponse : LiveData<Result<String>> = _friendInviteResponse
    private val _requestCheckResponse = MutableLiveData<Result<List<RequestCheckResponse>>>()
    val requestCheckResponse : LiveData<Result<List<RequestCheckResponse>>> = _requestCheckResponse
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

    fun loadFriendRanking(accessToken: String) {
        viewModelScope.launch {
            val result = repository.LoadFriendRank(accessToken)
            _friendRankingResponse.postValue(result)
        }
    }

    fun changeName(accessToken: String, name : String) {
        viewModelScope.launch {
            val request = ChangeNameRequest(name)
            val result = repository.ChangeName(accessToken, request)
            _changeNameResponse.postValue(result)
        }
    }

    fun myFriend(accessToken: String) {
        viewModelScope.launch {
            val result = repository.MyFriends(accessToken)
            _myFriendResponse.postValue(result)
        }
    }

    fun friendInvite(accessToken: String, name: String) {
        viewModelScope.launch {
            val request = FriendInviteRequest(name)
            val result = repository.FriendInvite(accessToken, request)
            _friendInviteResponse.postValue(result)
        }
    }

    fun checkRequest(accessToken: String) {
        viewModelScope.launch {
            val result = repository.CheckRequest(accessToken)
            _requestCheckResponse.postValue(result)
        }
    }
}