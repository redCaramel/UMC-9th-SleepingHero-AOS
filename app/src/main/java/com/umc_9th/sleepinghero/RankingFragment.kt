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
                val newList = mutableListOf<FriendRankingData>()
                for(item in data) {
                    val timeTotal : Int
                    if(item.totalSleepTime[1] == ':') timeTotal = item.totalSleepTime[0].toInt()
                    else timeTotal = item.totalSleepTime[1].toInt() * 10 + item.totalSleepTime[0].toInt()
                    newList.add(FriendRankingData(item.nickName, R.drawable.home_clear_hero, 1, 1, timeTotal, item.rank))
                }

            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
    }
}