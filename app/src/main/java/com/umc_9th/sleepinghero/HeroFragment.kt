package com.umc_9th.sleepinghero.ui.hero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.umc_9th.sleepinghero.R
import com.umc_9th.sleepinghero.WardrobeFragment
import com.umc_9th.sleepinghero.databinding.FragmentHeroBinding

class HeroFragment : Fragment() {

    private var _binding: FragmentHeroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 옷장 버튼 클릭
        binding.btnWardrobe.setOnClickListener {
            showWardrobe()
        }
    }

    private fun showWardrobe() {
        binding.wardrobeContainer.visibility = View.VISIBLE

        childFragmentManager.beginTransaction()
            .replace(R.id.wardrobeContainer, WardrobeFragment())
            .commit()
    }

    fun hideWardrobe() {
        val fragment =
            childFragmentManager.findFragmentById(R.id.wardrobeContainer)

        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        binding.wardrobeContainer.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
