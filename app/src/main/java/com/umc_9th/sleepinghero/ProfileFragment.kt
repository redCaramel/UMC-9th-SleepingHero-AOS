package com.umc_9th.sleepinghero

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentProfileBinding
import kotlin.getValue

class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
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
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        observeSocial()
        socialViewModel.myCharacter(TokenManager.getAccessToken(requireContext()).toString())
        binding.btnProfileBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        binding.btnProfileCancel.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        binding.etNicknameSetting.addTextChangedListener {
            binding.tvPreviewNickname.text = binding.etNicknameSetting.text

        }
        binding.btnProfileConfirm.setOnClickListener {
            socialViewModel.changeName(TokenManager.getAccessToken(requireContext()).toString(),
                binding.etNicknameSetting.text.toString().trim()
            )
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        return binding.root
    }
    private fun observeSocial() {
        socialViewModel.myCharResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.etNicknameSetting.setText(data.name)
                binding.tvPreviewNickname.text = data.name
                binding.tvPreviewLevel.text = "Lv.${data.currentLevel}"
                binding.tvInfoLevel.text = "Lv.${data.currentLevel}"
                socialViewModel.charSearch(TokenManager.getAccessToken(requireContext()).toString(), data.name)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
        socialViewModel.changeNameResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "적용 성공!", Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")
            }
        }
        socialViewModel.charSearchResponse.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data ->
                binding.tvPreviewStreak.text = "${data.continuousSleepDays}일"
                binding.tvPreviewHour.text = "${data.totalSleepHour}시간"
                binding.tvInfoStreak.text = "${data.continuousSleepDays}일"
                binding.tvInfoHour.text = "${data.totalSleepHour}시간"
                // TODO - 친구 인원수 구현
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")
            }
        }
    }
}