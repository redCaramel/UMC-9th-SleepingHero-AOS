package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.FriendItem
import com.umc_9th.sleepinghero.api.dto.FriendRankingItem
import com.umc_9th.sleepinghero.api.dto.FriendRequestItem
import com.umc_9th.sleepinghero.api.repository.FriendRepository
import kotlinx.coroutines.launch

class FriendViewModel(private val repository: FriendRepository) : ViewModel() {

    // 친구 요청 목록
    private val _friendRequests = MutableLiveData<Result<List<FriendRequestItem>>>()
    val friendRequests: LiveData<Result<List<FriendRequestItem>>> = _friendRequests

    // 친구 목록
    private val _friendsList = MutableLiveData<Result<List<FriendItem>>>()
    val friendsList: LiveData<Result<List<FriendItem>>> = _friendsList

    // 친구 랭킹
    private val _friendRanking = MutableLiveData<Result<List<FriendRankingItem>>>()
    val friendRanking: LiveData<Result<List<FriendRankingItem>>> = _friendRanking

    // 친구 요청 처리 결과
    private val _requestActionResult = MutableLiveData<Result<String>>()
    val requestActionResult: LiveData<Result<String>> = _requestActionResult

    /**
     * 대기 중인 친구 요청 목록 조회
     */
    fun loadFriendRequests(token: String) {
        viewModelScope.launch {
            val result = repository.getFriendRequests(token)
            _friendRequests.value = result
        }
    }

    /**
     * 친구 목록 조회
     */
    fun loadFriendsList(token: String) {
        viewModelScope.launch {
            val result = repository.getFriendsList(token)
            _friendsList.value = result
        }
    }

    /**
     * 친구 랭킹 조회
     */
    fun loadFriendRanking(token: String) {
        viewModelScope.launch {
            val result = repository.getFriendRanking(token)
            _friendRanking.value = result
        }
    }

    /**
     * 친구 요청 수락
     */
    fun approveFriendRequest(token: String, nickname: String) {
        viewModelScope.launch {
            val result = repository.approveFriendRequest(token, nickname)
            _requestActionResult.value = result
        }
    }

    /**
     * 친구 요청 거절
     */
    fun rejectFriendRequest(token: String, nickname: String) {
        viewModelScope.launch {
            val result = repository.rejectFriendRequest(token, nickname)
            _requestActionResult.value = result
        }
    }

    /**
     * 친구 요청 보내기
     */
    fun sendFriendRequest(token: String, nickname: String) {
        viewModelScope.launch {
            val result = repository.sendFriendRequest(token, nickname)
            _requestActionResult.value = result
        }
    }
}