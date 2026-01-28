package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.umc_9th.sleepinghero.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var currentHour = 8

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupCharacterCard()
        setupSleepTimeControl()
        setupButtons()

        return binding.root
    }

    // EXP 바 초기 설정
    private fun setupCharacterCard() {

        updateExpBar(100, 220)
    }

    // EXP 바의 너비
    private fun updateExpBar(currentExp: Int, maxExp: Int) {
        binding.progressExp.post {
            val containerWidth = binding.progressExpBg.width
            val progress = (currentExp.toFloat() / maxExp.toFloat())
            val progressWidth = (containerWidth * progress).toInt()

            val layoutParams = binding.progressExp.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.width = progressWidth
            binding.progressExp.layoutParams = layoutParams
        }

        // 텍스트 업데이트
        binding.tvExpCurrent.text = "$currentExp EXP"
        binding.tvExpMax.text = "$maxExp EXP"
    }

    private fun setupButtons() {
        binding.btnDiary.setOnClickListener {
            // TODO: 일지 화면으로 이동
        }

        // 수면 시작 버튼 - SleepTrackerFragment로 이동
        binding.btnStartSleep.setOnClickListener {
            val SleepTrackerFragment = SleepTrackerFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SleepTrackerFragment)
                .addToBackStack(null)
                .commit()
        }
    }


    // 초기 시간 설정
    private fun setupSleepTimeControl() {
        updateHourDisplay(currentHour)
        // 마이너스 버튼
        binding.btnMinus.setOnClickListener {
            if (currentHour > 1) {
                currentHour--
                updateHourDisplay(currentHour)
                binding.seekbarSleepTime.progress = currentHour
            }
        }

        // 플러스 버튼
        binding.btnPlus.setOnClickListener {
            if (currentHour < 12) {
                currentHour++
                updateHourDisplay(currentHour)
                binding.seekbarSleepTime.progress = currentHour
            }
        }

        // SeekBar 변경 리스너
        binding.seekbarSleepTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentHour = progress
                updateHourDisplay(currentHour)
                updateProgressLine(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateHourDisplay(hour: Int) {
        binding.tvHourDisplay.text = hour.toString()
        binding.tvSleepGoalTime.text = "${hour}시간"
    }

    private fun updateProgressLine(progress: Int) {
        // 보라색 Seekbar
        binding.progressLine.post {
            val seekBarWidth = binding.seekbarSleepTime.width
            val maxProgress = binding.seekbarSleepTime.max
            val progressWidth = (seekBarWidth * progress) / maxProgress

            val layoutParams = binding.progressLine.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.width = progressWidth
            binding.progressLine.layoutParams = layoutParams
        }
    }
}