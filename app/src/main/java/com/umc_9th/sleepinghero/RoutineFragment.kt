package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc_9th.sleepinghero.databinding.FragmentHomeBinding
import com.umc_9th.sleepinghero.databinding.FragmentRoutineBinding

class RoutineFragment : Fragment() {
    private lateinit var binding: FragmentRoutineBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }
}