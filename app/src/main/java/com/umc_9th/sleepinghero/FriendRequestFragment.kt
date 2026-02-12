package com.umc_9th.sleepinghero

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
                socialViewModel.responseRequest(TokenManager.getAccessToken(requireContext()).toString(), "APPROVE", heroData.name)
                adapter.removeItem(heroData)
            },
            rejectEvent = {heroData ->
                socialViewModel.responseRequest(TokenManager.getAccessToken(requireContext()).toString(), "REJECTED", heroData.name)
                adapter.removeItem(heroData)
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
                    data.skinId,
                    data.continuousSleepDays,
                    data.totalSleepHour
                )
                adapter.updateList(info)
                binding.textView36.text = "답변되지 않은 친구 요청 ${adapter.itemCount}건"
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "탐색 실패: $message")

            }
        }
        socialViewModel.responseRequestResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "친구 요청을 처리하였습니다!", Toast.LENGTH_SHORT).show()
                binding.textView36.text = "답변되지 않은 친구 요청 ${adapter.itemCount}건"
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "탐색 실패: $message")
            }
        }
    }
}