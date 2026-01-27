package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc_9th.sleepinghero.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
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
        binding.btnProfileConfirm.setOnClickListener {
            // TODO - 프로필 변경사항 적용
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        return binding.root
    }
}