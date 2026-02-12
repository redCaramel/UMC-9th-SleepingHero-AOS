package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc_9th.sleepinghero.databinding.FragmentWardrobeBinding
import com.umc_9th.sleepinghero.ui.hero.HeroFragment

class WardrobeFragment : Fragment() {

    private lateinit var binding: FragmentWardrobeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
