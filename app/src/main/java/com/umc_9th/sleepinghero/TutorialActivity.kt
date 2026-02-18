package com.umc_9th.sleepinghero

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.TutorialRepository
import com.umc_9th.sleepinghero.api.viewmodel.TutorialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.TutorialViewModelFactory
import com.umc_9th.sleepinghero.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding

    private val tutorialRepository by lazy {
        TutorialRepository(ApiClient.tutorialService)
    }

    private val tutorialViewModel: TutorialViewModel by viewModels {
        TutorialViewModelFactory(tutorialRepository)
    }

    private val pages = listOf(
        TutorialPage(R.drawable.tutorial_1, "홈에서 대시보드를 확인하고 수면을 기록해요"),
        TutorialPage(R.drawable.tutorial_2, "용사에서 용사 정보와 최근 수면 기록을 확인해요"),
        TutorialPage(R.drawable.tutorial_3, "루틴에서 주간/월간 수면 패턴을 확인해요"),
        TutorialPage(R.drawable.tutorial_4, "소셜에서 친구들과 용사를 공유해요")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeFinishTutorial()

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
            val token = TokenManager.getAccessToken(this).orEmpty()
            if (token.isBlank()) {
                startActivity(Intent(this, StartActivity::class.java))
                finish()
                return@setOnClickListener
            }

            tutorialViewModel.finishTutorial(token)
        }
    }

    private fun observeFinishTutorial() {
        tutorialViewModel.tutorialFinish.observe(this) { result ->
            result.onSuccess {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure { e ->
                Toast.makeText(this, "튜토리얼 완료 처리 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
}
