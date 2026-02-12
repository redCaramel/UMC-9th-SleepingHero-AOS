package com.umc_9th.sleepinghero

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
import com.umc_9th.sleepinghero.databinding.FragmentRankingBinding
import kotlin.getValue

class RankingFragment : Fragment() {
    private lateinit var binding: FragmentRankingBinding
    private lateinit var adapter : FriendRankingAdapter
    private var count = 1
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
        binding = FragmentRankingBinding.inflate(inflater, container, false)
        adapter = FriendRankingAdapter(mutableListOf<FriendRankingData>())
        binding.rankingContainer.adapter = adapter
        binding.rankingContainer.layoutManager = LinearLayoutManager(requireContext())
        observeRanking()
        socialViewModel.loadFriendRanking(TokenManager.getAccessToken(requireContext()).toString())
        return binding.root
    }

    private fun observeRanking() {
        socialViewModel.friendRankingResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                data.forEach { q->
                    Log.d("test", "${q.nickName}")
                }

                for(item in data) {
                    socialViewModel.charSearch(TokenManager.getAccessToken(requireContext()).toString(), item.nickName)

                }

            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
        socialViewModel.charSearchResponse.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data->
                val item = FriendRankingData(data.heroName, data.skinId, data.level, data.continuousSleepDays, data.totalSleepHour, count)
                count++
                adapter.uploadList(item)
            }
        }
    }
}