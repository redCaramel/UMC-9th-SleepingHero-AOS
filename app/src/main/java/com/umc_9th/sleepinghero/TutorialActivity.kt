package com.umc_9th.sleepinghero

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.umc_9th.sleepinghero.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding

    private val pages = listOf(
        TutorialPage(R.drawable.tutorial_1, "홈에서 대시보드와 기록을 확인해요"),
        TutorialPage(R.drawable.tutorial_2, "루틴에서 주간/월간 수면 패턴을 확인해요"),
        TutorialPage(R.drawable.tutorial_3, "소셜에서 친구들과 용사를 공유해요")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }


        binding.viewPager.adapter = TutorialAdapter(pages)

        setupDots(pages.size)
        updateDots(0)
        binding.btnStart.isEnabled = false

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                binding.btnStart.isEnabled = (position == pages.lastIndex)
            }
        })

        binding.btnStart.setOnClickListener {
            markTutorialDone()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupDots(count: Int) {
        binding.dotsLayout.removeAllViews()
        repeat(count) {
            val dot = TextView(this).apply {
                text = "●"
                textSize = 10f
                setPadding(8, 0, 8, 0)
                setTextColor(Color.parseColor("#D1D5DB")) // 기본 회색
            }
            binding.dotsLayout.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        for (i in 0 until binding.dotsLayout.childCount) {
            val tv = binding.dotsLayout.getChildAt(i) as TextView
            tv.setTextColor(
                if (i == selected) Color.parseColor("#111827") else Color.parseColor("#D1D5DB")
            )
        }
    }

    private fun markTutorialDone() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("tutorial_done", true)
            .apply()
    }

    companion object {
        fun isTutorialDone(context: Context): Boolean {
            return context.getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("tutorial_done", false)
        }
    }
}
