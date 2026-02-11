package com.umc_9th.sleepinghero

import android.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentFriendRequestBinding
import kotlin.getValue


class FriendRequestFragment : Fragment() {

    private lateinit var binding: FragmentFriendRequestBinding
    private lateinit var adapter : FriendRequestAdapter
    val requestList = mutableListOf<HeroData>()
    private val socialRepository by lazy {
        SocialRepository(ApiClient.socialService)
    }
    private val socialViewModel : SocialViewModel by viewModels(
        factoryProducer = { SocialViewModelFactory(socialRepository) }
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        observeRequest()
        adapter = FriendRequestAdapter(requestList,
            acceptEvent = {heroData ->
                // TODO - 수락/거절
            },
            rejectEvent = {heroData ->

            }
        )
        binding.heroContainer.adapter = adapter
        binding.heroContainer.layoutManager = LinearLayoutManager(requireContext())
        socialViewModel.checkRequest(TokenManager.getAccessToken(requireContext()).toString())
        return binding.root
    }
    private fun observeRequest() {
        socialViewModel.requestCheckResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                requestList.clear()
                data.forEach { hero ->
                    Log.d("test", "요청 감지 - ${hero.nickname}")
                    socialViewModel.charSearch(TokenManager.getAccessToken(requireContext()).toString(), hero.nickname)
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
        socialViewModel.charSearchResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                val info = HeroData(
                    data.heroName,
                    data.level,
                    R.drawable.ic_delete,
                    data.continuousSleepDays,
                    data.totalSleepHour
                )
                requestList.add(info)
                adapter.updateList(requestList)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "탐색 실패: $message")

            }
        }
    }
}