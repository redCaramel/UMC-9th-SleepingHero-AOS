package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc_9th.sleepinghero.api.dto.TutorialStatusResponse
import com.umc_9th.sleepinghero.api.dto.TutorialUpdateResponse
import com.umc_9th.sleepinghero.api.repository.TutorialRepository
import kotlinx.coroutines.launch

class TutorialViewModel(private val repository: TutorialRepository) : ViewModel() {

    private val _tutorialStatus = MutableLiveData<Result<TutorialStatusResponse>>()
    val tutorialStatus: LiveData<Result<TutorialStatusResponse>> = _tutorialStatus

    private val _tutorialFinish = MutableLiveData<Result<TutorialUpdateResponse>>()
    val tutorialFinish: LiveData<Result<TutorialUpdateResponse>> = _tutorialFinish

    fun getTutorial(accessToken: String) {
        viewModelScope.launch {
            _tutorialStatus.postValue(repository.getTutorial(accessToken))
        }
    }

    fun finishTutorial(accessToken: String) {
        viewModelScope.launch {
            _tutorialFinish.postValue(repository.finishTutorial(accessToken))
        }
    }
}
