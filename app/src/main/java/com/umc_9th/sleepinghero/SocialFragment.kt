package com.umc_9th.sleepinghero

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.umc_9th.sleepinghero.databinding.FragmentSocialBinding
import androidx.core.graphics.toColorInt

class SocialFragment : Fragment() {
    private lateinit var binding: FragmentSocialBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectTab(binding.tabFriend)
        setupTabs()
    }

    private fun setupTabs() {

        val tabs = listOf(
            Triple(binding.tabFriend, binding.textMyFriends, binding.iconMyFriends),
            Triple(binding.tabGroup, binding.textGroup, binding.iconGroup),
            Triple(binding.tabRecommend, binding.textRecommend, binding.iconRecommend)
        )

        tabs.forEach { (layout, text, icon) ->
            layout.setOnClickListener {
                selectTab(layout)
            }
        }

        selectTab(binding.tabFriend)
    }

    private fun selectTab(selectedLayout: LinearLayout) {

        val tabs = listOf(
            Triple(binding.tabFriend, binding.textMyFriends, binding.iconMyFriends),
            Triple(binding.tabGroup, binding.textGroup, binding.iconGroup),
            Triple(binding.tabRecommend, binding.textRecommend, binding.iconRecommend)
        )

        tabs.forEach { (layout, text, icon) ->

            if (layout == selectedLayout) {
                layout.setBackgroundResource(R.drawable.tab_selected)
                text.setTextColor(Color.parseColor("#FFFFFF"))
                icon.setColorFilter(Color.parseColor("#FFFFFF"))
            } else {
                layout.setBackgroundResource(R.drawable.tab_unselected)
                text.setTextColor(Color.parseColor("#666666"))
                icon.setColorFilter(Color.parseColor("#666666"))
            }
        }
    }


}