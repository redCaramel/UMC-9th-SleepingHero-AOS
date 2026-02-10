package com.umc_9th.sleepinghero

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.umc_9th.sleepinghero.databinding.ActivityCreateGroupBinding

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreateGroupBinding
    private lateinit var iconList : List<View>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iconList = listOf(
            binding.imgGroupIconA,
            binding.imgGroupIconB,
            binding.imgGroupIconC,
            binding.imgGroupIconD,
            binding.imgGroupIconE,
            binding.imgGroupIconF,
            binding.imgGroupIconG,
            binding.imgGroupIconH,
            binding.imgGroupIconI,
            binding.imgGroupIconJ,
            binding.imgGroupIconK,
            binding.imgGroupIconL
        )
        iconList.forEach { icon ->
            Log.d("tests", "${icon.isSelected}")
            icon.setOnClickListener {
                updateSelection(icon)
            }
        }
        binding.scrollView3.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        updateSelection(binding.imgGroupIconA)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

    }
    private fun updateSelection(selectedIcon: View) {
        iconList.forEach { it.isSelected = false }
        selectedIcon.isSelected = true
        Log.d("tests", "${selectedIcon.id}")
    }
}