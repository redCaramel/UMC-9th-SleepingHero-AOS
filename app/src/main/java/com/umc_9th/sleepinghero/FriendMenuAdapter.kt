package com.umc_9th.sleepinghero

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FriendMenuAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() : Int {
        return 3;
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyFriendFragment()
            1 -> FriendRequestFragment()
            2 -> GroupRequestCheckFragment()
            else -> MyFriendFragment()
        }
    }
}