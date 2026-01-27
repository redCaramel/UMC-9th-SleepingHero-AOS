package com.umc_9th.sleepinghero

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.umc_9th.sleepinghero.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private lateinit var binding : FragmentSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectTab(binding.tabUnuse)
        setupTabs()
        binding.swNodistract.setOnCheckedChangeListener { CompoundButton, onSwitch ->
            if(onSwitch) {
                binding.cardNodistractSetting.visibility=View.VISIBLE
            }
            else {
                binding.cardNodistractSetting.visibility=View.GONE
            }
        }
    }

    private fun setupTabs() {

        val tabs = listOf(
            Pair(binding.tabUnuse, binding.tvUnuse),
            Pair(binding.tabSound, binding.tvSound),
            Pair(binding.tabVibrate, binding.tvVibrate)
        )

        tabs.forEach { (layout, text) ->
            layout.setOnClickListener {
                selectTab(layout)
            }
        }

        selectTab(binding.tabUnuse)
    }

    private fun selectTab(selectedLayout: LinearLayout) {

        val tabs = listOf(
            Pair(binding.tabUnuse, binding.tvUnuse),
            Pair(binding.tabSound, binding.tvSound),
            Pair(binding.tabVibrate, binding.tvVibrate)
        )

        tabs.forEach { (layout, text) ->

            if (layout == selectedLayout) {
                layout.setBackgroundResource(R.drawable.tab_selected)
                text.setTextColor(Color.parseColor("#FFFFFF"))
                if(layout == binding.tabSound) binding.cardAlarmVolume.visibility = View.VISIBLE
                else binding.cardAlarmVolume.visibility = View.GONE
            } else {
                layout.setBackgroundResource(R.drawable.tab_unselected)
                text.setTextColor(Color.parseColor("#666666"))
            }
        }
    }


}