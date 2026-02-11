package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository

class SocialViewModelFactory(private val repository: SocialRepository) :
    ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}