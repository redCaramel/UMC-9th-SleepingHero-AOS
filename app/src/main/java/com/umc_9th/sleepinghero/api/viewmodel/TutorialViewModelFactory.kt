package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.umc_9th.sleepinghero.api.repository.TutorialRepository

class TutorialViewModelFactory(
    private val repository: TutorialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TutorialViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
