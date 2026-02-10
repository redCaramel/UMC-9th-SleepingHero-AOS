package com.umc_9th.sleepinghero

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.session.MediaSession
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.umc_9th.sleepinghero.databinding.FragmentSocialBinding
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import kotlin.getValue

class SocialFragment : Fragment() {
    private lateinit var binding: FragmentSocialBinding
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
        binding = FragmentSocialBinding.inflate(inflater, container, false)
        observeSocial()
        socialViewModel.myCharacter(TokenManager.getAccessToken(requireContext()).toString())
        binding.btnProfile.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name",binding.tvSocialName.text.toString())
                putString("time",binding.tvSocialTotal.text.toString())
                putString("streak",binding.tvSocialStreak.text.toString())
                putString("level",binding.tvSocialLevel.text.toString())
                putString("friend", binding.tvSocialFriend.text.toString())
            }
            ProfileFragment().arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.tabFriend.setOnClickListener {
            val bundle = Bundle().apply {
            }
            RelationFragment().arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, RelationFragment())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectTab(binding.tabSearch)
        setupTabs()
    }

    private fun initText() {

    }
    private fun setupTabs() {

        val tabs = listOf(
            Triple(binding.tabSearch, binding.textMyFriends, binding.iconMyFriends),
            Triple(binding.tabGroup, binding.textGroup, binding.iconGroup),
            Triple(binding.tabRanking, binding.textRecommend, binding.iconRecommend)
        )

        tabs.forEach { (layout, text, icon) ->
            layout.setOnClickListener {
                selectTab(layout)
            }
        }

        selectTab(binding.tabSearch)
        replaceInnerFragment(FriendFragment())
    }

    private fun selectTab(selectedLayout: LinearLayout) {

        val tabs = listOf(
            Triple(binding.tabSearch, binding.textMyFriends, binding.iconMyFriends),
            Triple(binding.tabGroup, binding.textGroup, binding.iconGroup),
            Triple(binding.tabRanking, binding.textRecommend, binding.iconRecommend)
        )

        tabs.forEach { (layout, text, icon) ->

            if (layout == selectedLayout) {
                layout.setBackgroundResource(R.drawable.tab_selected)
                text.setTextColor(Color.parseColor("#FFFFFF"))
                icon.setColorFilter(Color.parseColor("#FFFFFF"))
                if(layout == binding.tabSearch) {
                    replaceInnerFragment(FriendFragment())
                }
                else if(layout == binding.tabGroup) {
                    replaceInnerFragment(GroupFragment())
                }
                else {
                    replaceInnerFragment(RankingFragment())
                }
            } else {
                layout.setBackgroundResource(R.drawable.tab_unselected)
                text.setTextColor(Color.parseColor("#666666"))
                icon.setColorFilter(Color.parseColor("#666666"))
            }
        }
    }
    private fun replaceInnerFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.container_social, fragment)
            .commit()
    }

    private fun observeSocial() {
        socialViewModel.myCharResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.tvSocialName.text = data.name
                binding.tvSocialLevel.text = "Lv.${data.currentLevel}"

            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
    }
}