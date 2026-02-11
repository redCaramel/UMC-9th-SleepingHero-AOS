package com.umc_9th.sleepinghero

import android.R
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
import com.umc_9th.sleepinghero.api.repository.GroupRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.GroupViewModel
import com.umc_9th.sleepinghero.api.viewmodel.GroupViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentGroupRequestBinding
import kotlin.getValue

class GroupRequestFragment : Fragment() {
    private lateinit var binding : FragmentGroupRequestBinding
    private lateinit var adapter: HeroAdapter
    val friendList = mutableListOf<HeroData>()
    private val socialRepository by lazy {
        SocialRepository(ApiClient.socialService)
    }
    private val socialViewModel : SocialViewModel by viewModels(
        factoryProducer = { SocialViewModelFactory(socialRepository) }
    )
    private val groupRepository by lazy {
        GroupRepository(ApiClient.groupService)
    }
    private val groupViewModel : GroupViewModel by viewModels(
        factoryProducer = { GroupViewModelFactory(groupRepository) }
    )
    private var groupName : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupName = it.getString("name").toString()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupRequestBinding.inflate(inflater, container, false)
        observeFriend()
        observeGroup()
        groupViewModel.groupRank(TokenManager.getAccessToken(requireContext()).toString(), groupName)
        binding.btnBackRequest.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.umc_9th.sleepinghero.R.id.container_main, SocialFragment())
                .commit()
        }
        adapter = HeroAdapter(friendList,
            clickEvent = {heroData ->
                //TODO - 그룹 초대 구현
            },
            1)
        binding.rankingContainer.adapter = adapter
        binding.rankingContainer.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    private fun observeGroup() {
        groupViewModel.groupRankResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.tvGroupName.text = data.groupName
                binding.tvGroupInfo.text = data.description
                binding.tvGroupLeader.text = data.groupMasterNickname
                binding.tvGroupTotal.text = "${data.totalGroupSleepTime}시간"
                binding.tvGroupStreak.text = "${data.averageConsecutiveDays}일"
                binding.tvGroupPeople.text = "${data.totalMembers}명"
                var iconList = listOf(
                    com.umc_9th.sleepinghero.R.drawable.ic_group_a,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_b,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_c,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_d,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_e,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_f,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_g,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_h,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_i,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_j,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_k,
                    com.umc_9th.sleepinghero.R.drawable.ic_group_l
                )
                binding.imgGroupIcon.setImageResource(iconList[data.groupImageId])
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "생성 실패: $message")
                Toast.makeText(
                    requireContext(),
                    "요청 전송 실패 - $message",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                    data.skinId,
                    data.continuousSleepDays,
                    data.totalSleepHour
                )
                adapter.updateList(friendList)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "탐색 실패: $message")

            }
        }
    }

}