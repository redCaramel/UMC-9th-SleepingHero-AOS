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
import com.umc_9th.sleepinghero.api.repository.GroupRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.GroupViewModel
import com.umc_9th.sleepinghero.api.viewmodel.GroupViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentGroupRequestCheckBinding
import kotlin.getValue


class GroupRequestCheckFragment : Fragment() {

    private lateinit var binding: FragmentGroupRequestCheckBinding
    private lateinit var adapter : GroupRequestAdapter
    val requestList = mutableListOf<GroupData>()
    private val groupRepository by lazy {
        GroupRepository(ApiClient.groupService)
    }
    private val groupViewModel : GroupViewModel by viewModels(
        factoryProducer = { GroupViewModelFactory(groupRepository) }
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupRequestCheckBinding.inflate(inflater, container, false)
        observeRequest()
        adapter = GroupRequestAdapter(requestList,
            acceptEvent = {groupData ->
                groupViewModel.groupRequest(TokenManager.getAccessToken(requireContext()).toString(), "APPROVE", groupData.groupName)
                adapter.removeItem(groupData)
            },
            rejectEvent = {groupData ->
                groupViewModel.groupRequest(TokenManager.getAccessToken(requireContext()).toString(), "REJECTED", groupData.groupName)
                adapter.removeItem(groupData)
            }
        )
        binding.groupContainer.adapter = adapter
        binding.groupContainer.layoutManager = LinearLayoutManager(requireContext())
        groupViewModel.groupRequestCheck(TokenManager.getAccessToken(requireContext()).toString())
        return binding.root
    }
    private fun observeRequest() {
        groupViewModel.groupRequestCheckResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                data.forEach { group ->
                    Log.d("test", "요청 감지 - ${group.groupName}")
                    groupViewModel.groupRank(TokenManager.getAccessToken(requireContext()).toString(), group.groupName)
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
        groupViewModel.groupRankResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                val info = GroupData(
                    data.groupName,
                    data.description,
                    data.totalMembers,
                    data.totalGroupSleepTime,
                    data.averageConsecutiveDays,
                    data.groupMasterNickname,
                    data.groupImageId.toLong(),
                    data.memberRankings
                )
                adapter.uploadList(info)
                binding.textView36.text = "답변되지 않은 그룹 초대 ${adapter.itemCount}건"
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "탐색 실패: $message")

            }
        }
        groupViewModel.groupRequestResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "그룹 초대에 응답했습니다!", Toast.LENGTH_SHORT).show()
                binding.textView36.text = "답변되지 않은 그룹 초대 ${adapter.itemCount}건"
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "응답 실패: $message")
            }
        }
    }
}