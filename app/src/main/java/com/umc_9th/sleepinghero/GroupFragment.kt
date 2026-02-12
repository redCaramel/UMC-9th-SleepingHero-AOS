package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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
import com.umc_9th.sleepinghero.databinding.ActivityCreateGroupBinding
import com.umc_9th.sleepinghero.databinding.ActivityGroupInfoBinding
import com.umc_9th.sleepinghero.databinding.FragmentGroupBinding
import kotlin.getValue

class GroupFragment : Fragment() {
    private lateinit var binding: FragmentGroupBinding
    private lateinit var adapter: GroupAdapter
    private lateinit var grAdapter : GroupRankingAdapter
    lateinit var mainActivity: MainActivity
    val GroupList = mutableListOf<GroupData>()
    val GroupMemberList = mutableListOf<GroupRankingData>()
    private val groupRepository by lazy {
        GroupRepository(ApiClient.groupService)
    }
    private val groupViewModel : GroupViewModel by viewModels(
        factoryProducer = { GroupViewModelFactory(groupRepository) }
    )
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        observeGroup()
        adapter = GroupAdapter(GroupList,
            detailEvent = {groupData ->
                val dialogBinding = ActivityGroupInfoBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
                    .setView(dialogBinding.root)
                    .setTitle("그룹 상세정보")
                    .create()
                dialogBinding.tvGroupName.text = groupData.groupName
                dialogBinding.tvGroupInfo.text = groupData.description
                var iconList = listOf(
                    R.drawable.ic_group_a,
                    R.drawable.ic_group_b,
                    R.drawable.ic_group_c,
                    R.drawable.ic_group_d,
                    R.drawable.ic_group_e,
                    R.drawable.ic_group_f,
                    R.drawable.ic_group_g,
                    R.drawable.ic_group_h,
                    R.drawable.ic_group_i,
                    R.drawable.ic_group_j,
                    R.drawable.ic_group_k,
                    R.drawable.ic_group_l
                )
                dialogBinding.imgGroupIcon.setImageResource(iconList[groupData.icon.toInt()])
                dialogBinding.tvGroupPeople.text = "${groupData.totalMembers}명"
                dialogBinding.tvGroupTotal.text = "${groupData.totalTime}시간"
                dialogBinding.tvGroupStreak.text = "${groupData.streak}일"
                dialogBinding.tvGroupMember.text = "멤버 (${groupData.totalMembers})"

                grAdapter = GroupRankingAdapter(GroupMemberList)
                dialogBinding.heroContainer.adapter = grAdapter
                dialogBinding.heroContainer.layoutManager = LinearLayoutManager(requireContext())
                dialog.show()
            },
            inviteEvent =  { groupData ->
                val fragment = GroupRequestFragment()
                val bundle = Bundle().apply {
                    putString("name",groupData.groupName)
                }
                fragment.arguments = bundle
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container_main, fragment)
                    .addToBackStack(null)
                    .commit()
            })
        binding.groupContainer.adapter = adapter
        binding.groupContainer.layoutManager = LinearLayoutManager(requireContext())
        groupViewModel.groupCheck(TokenManager.getAccessToken(requireContext()).toString())
        binding.btnCreateGroup.setOnClickListener {
            val dialogBinding = ActivityCreateGroupBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
                .setView(dialogBinding.root)
                .setTitle("새 그룹 만들기")
                .create()
            var iconList : List<View> = listOf(
                dialogBinding.imgGroupIconA,
                dialogBinding.imgGroupIconB,
                dialogBinding.imgGroupIconC,
                dialogBinding.imgGroupIconD,
                dialogBinding.imgGroupIconE,
                dialogBinding.imgGroupIconF,
                dialogBinding.imgGroupIconG,
                dialogBinding.imgGroupIconH,
                dialogBinding.imgGroupIconI,
                dialogBinding.imgGroupIconJ,
                dialogBinding.imgGroupIconK,
                dialogBinding.imgGroupIconL
            )
            iconList.forEach { icon ->
                Log.d("tests", "${icon.isSelected}")
                icon.setOnClickListener {
                    iconList.forEach { it.isSelected = false }
                    icon.isSelected = true
                    val img = icon as ImageView
                    dialogBinding.imgPreviewIcon.setImageDrawable(img.drawable)
                    Log.d("tests", "${icon.id}")
                }
            }
            iconList.forEach { it.isSelected = false }
            dialogBinding.imgGroupIconA.isSelected = true
            val img = dialogBinding.imgGroupIconA
            dialogBinding.imgPreviewIcon.setImageDrawable(img.drawable)

            dialogBinding.etGroupName.addTextChangedListener { editText ->
                dialogBinding.tvPreviewName.text = dialogBinding.etGroupName.text
            }
            dialogBinding.etGroupInfo.addTextChangedListener { editText ->
                dialogBinding.tvPreviewInfo.text = dialogBinding.etGroupInfo.text
            }
            dialogBinding.btnCreateGroupCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.btnCreateGroupConfirm.setOnClickListener {
                var iconNum = iconList.indexOfFirst { it.isSelected }
                groupViewModel.createGroup(TokenManager.getAccessToken(requireContext()).toString(), dialogBinding.etGroupName.text.toString(), dialogBinding.etGroupInfo.text.toString(), 30, iconNum)
                dialog.dismiss()
            }
            dialog.show()
        }

        return binding.root
    }
    private fun observeGroup() {
        groupViewModel.createGroupResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                Toast.makeText(
                    requireContext(),
                    "그룹 생성을 완료했습니다!",
                    Toast.LENGTH_SHORT
                ).show()
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
        groupViewModel.groupCheckResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                if(data.isEmpty()) binding.viewNoGroup.visibility=View.VISIBLE
                else binding.viewNoGroup.visibility=View.GONE
                GroupList.clear()
                data.forEach { group ->
                    Log.d("test", "그룹 조회 성공 - ${group.name}")
                    groupViewModel.groupRank(TokenManager.getAccessToken(requireContext()).toString(), group.name)
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "그룹 조회 실패: $message")
            }
        }
        groupViewModel.groupRankResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                Log.d("test", "상세 조회 성공- ${data.groupName}")
                val group = GroupData(
                    data.groupName,
                    data.description,
                    data.totalMembers,
                    data.totalGroupSleepTime,
                    data.averageConsecutiveDays,
                    data.groupMasterNickname,
                    data.groupImageId.toLong()
                )
                GroupList.add(group)
                adapter.updateList(GroupList.toList())
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "상세 조회 실패: $message")
            }
        }
    }
}