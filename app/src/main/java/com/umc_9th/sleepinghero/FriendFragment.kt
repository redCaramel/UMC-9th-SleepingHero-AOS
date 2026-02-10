package com.umc_9th.sleepinghero

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.SettingViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SettingViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentFriendBinding
import kotlin.getValue

class FriendFragment : Fragment() {
    private lateinit var binding: FragmentFriendBinding
    private lateinit var adapter : HeroAdapter
    val searchedHeroList = mutableListOf<HeroData>()
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
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        observeSearch()
        observeInvite()

        adapter = HeroAdapter(searchedHeroList,
            clickEvent =  { heroData ->
                Log.d("test", heroData.name)
                socialViewModel.friendInvite(TokenManager.getAccessToken(requireContext()).toString(), heroData.name)
            },
            1)

        binding.heroContainer.adapter = adapter
        binding.heroContainer.layoutManager = LinearLayoutManager(requireContext())
        binding.etHeroSearch.addTextChangedListener { text ->
            socialViewModel.charSearch(TokenManager.getAccessToken(requireContext()).toString(), text.toString())
        }
        return binding.root
    }
    private fun observeSearch() {
        socialViewModel.charSearchResponse.observe(viewLifecycleOwner) { result ->
            //Result -> status, code 등이 있고 이 안 data에 값이 존재
            result.onSuccess { data ->
                val query = data.heroName
                val newList = mutableListOf<HeroData>()
                if (query.isNotEmpty()) {
                    newList.add(HeroData(query, data.level, R.drawable.ic_delete, data.continuousSleepDays, data.totalSleepHour)) //TODO - 용사 아이콘 적용
                }
                adapter.updateList(newList)
                binding.viewNoHero.visibility = View.GONE
            }.onFailure { error ->
                binding.viewNoHero.visibility = View.VISIBLE
                val newList = mutableListOf<HeroData>()
                adapter.updateList(newList)
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "연결 실패: $message")
            }
        }
    }
    private fun observeInvite() {
        socialViewModel.friendInviteResponse.observe(viewLifecycleOwner) { result ->
            //Result -> status, code 등이 있고 이 안 data에 값이 존재
            result.onSuccess { data ->
                Toast.makeText(
                    requireContext(),
                    "친구 요청을 전송했습니다!",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->

                val newList = mutableListOf<HeroData>()
                adapter.updateList(newList)
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "연결 실패: $message")
                Toast.makeText(
                    requireContext(),
                    "요청 전송 실패 - $message",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}