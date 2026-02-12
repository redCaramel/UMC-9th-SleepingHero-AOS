package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.umc_9th.sleepinghero.databinding.FragmentRelationBinding

class RelationFragment : Fragment() {
   private lateinit var binding: FragmentRelationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRelationBinding.inflate(inflater, container, false)
        val contentAdapter = FriendMenuAdapter(this)
        binding.vpFriendMenu.adapter = contentAdapter
        binding.vpFriendMenu.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        TabLayoutMediator(binding.tblFriendMenu, binding.vpFriendMenu) {tab, position ->
            tab.text = when(position) {
                0 -> "내 친구"
                1 -> "받은 친구 요청"
                2 -> "받은 그룹 초대"
                else -> ""
            }
        }.attach()
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        return binding.root
    }

}