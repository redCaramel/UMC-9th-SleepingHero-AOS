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
import com.umc_9th.sleepinghero.databinding.FragmentMyFriendBinding
import kotlin.getValue

class MyFriendFragment : Fragment() {
   private lateinit var binding : FragmentMyFriendBinding
   private lateinit var adapter : HeroAdapter
   val friendList = mutableListOf<HeroData>()
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
        binding = FragmentMyFriendBinding.inflate(inflater, container, false)
        observeFriend()
        adapter = HeroAdapter(friendList,
            clickEvent = { heroData ->
                // TODO - 친삭 구현
            },
            2)
        binding.heroContainer.adapter = adapter
        binding.heroContainer.layoutManager = LinearLayoutManager(requireContext())
        socialViewModel.myFriend(TokenManager.getAccessToken(requireContext()).toString())
        return binding.root
    }
    private fun observeFriend() {
        socialViewModel.myFriendResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
               friendList.clear()
                data.forEach { hero ->
                    socialViewModel.charSearch(TokenManager.getAccessToken(requireContext()).toString(), hero.nickName)
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
                friendList.add(info)
                adapter.updateList(friendList)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "탐색 실패: $message")

            }
        }
    }
}