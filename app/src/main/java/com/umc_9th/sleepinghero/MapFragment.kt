package com.umc_9th.sleepinghero

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.CharacterRepository
import com.umc_9th.sleepinghero.api.viewmodel.CharacterViewModel
import com.umc_9th.sleepinghero.api.viewmodel.CharacterViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentMapBinding
import kotlin.math.max

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var characterViewModel: CharacterViewModel

    private var currentHeroLevel: Int = 1

    // UI overlay들
    private val clearedLevelMarkers = mutableListOf<ImageView>() // 파란 점들
    private var currentLevelMarker: ImageView? = null            // 보라 점
    private var locationLabel: ImageView? = null                 // "현재 위치" 라벨

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        observeViewModel()
        loadCharacterInfo()

        binding.btnMinimize.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, HomeFragment())
                .commit()
        }
    }

    private fun initViewModel() {
        val repository = CharacterRepository(ApiClient.characterService)
        val factory = CharacterViewModelFactory(repository)
        characterViewModel = ViewModelProvider(this, factory)[CharacterViewModel::class.java]
    }

    private fun loadCharacterInfo() {
        val token = TokenManager.getAccessToken(requireContext())
        if (!token.isNullOrEmpty()) {
            characterViewModel.loadCharacterInfo(token)
        } else {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            currentHeroLevel = 1
            setupMapUI()
        }
    }

    private fun observeViewModel() {
        characterViewModel.characterInfo.observe(viewLifecycleOwner) { result ->
            result.onSuccess { characterInfo ->
                currentHeroLevel = characterInfo.currentLevel.coerceIn(1, 100)
                setupMapUI()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "캐릭터 정보 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                currentHeroLevel = 1
                setupMapUI()
            }
        }
    }

    private fun setupMapUI() {
        // ivMapPath가 레이아웃 완료된 뒤에 좌표 계산 가능
        binding.ivMapPath.post {
            setupClearedLevelMarkers()
            setupCurrentLevelMarker()
            setupLocationLabel()
            scrollToHeroLevel(animated = true)
        }
    }

    /**
     * 레벨 점 중심(px) 좌표 반환 (ivMapPath 표시 크기 기준)
     */
    private fun getLevelCenterPx(level: Int): Pair<Int, Int> {
        val lv = level.coerceIn(1, 100)
        val w = binding.ivMapPath.width
        val h = binding.ivMapPath.height
        val x = (w * LEVEL_X_RATIO[lv]).toInt()
        val y = (h * LEVEL_Y_RATIO[lv]).toInt()
        return x to y
    }

    /**
     * 1 ~ 현재레벨-1 파란 점 마커들
     */
    private fun setupClearedLevelMarkers() {
        val mapContainer = binding.ivMapPath.parent as? ConstraintLayout ?: return

        clearedLevelMarkers.forEach { mapContainer.removeView(it) }
        clearedLevelMarkers.clear()

        val size = 12.dpToPx()

        for (level in 1 until currentHeroLevel) {
            val (cx, cy) = getLevelCenterPx(level)

            val marker = ImageView(requireContext()).apply {
                setImageResource(R.drawable.map_blue_circle)
                id = View.generateViewId()
                layoutParams = ConstraintLayout.LayoutParams(size, size).apply {
                    startToStart = binding.ivMapPath.id
                    topToTop = binding.ivMapPath.id
                    marginStart = (cx - size / 2).coerceAtLeast(0)
                    topMargin = (cy - size / 2).coerceAtLeast(0)
                }
            }

            mapContainer.addView(marker)
            clearedLevelMarkers.add(marker)
        }
    }

    /**
     * 현재 레벨 보라 점 마커
     */
    private fun setupCurrentLevelMarker() {
        val mapContainer = binding.ivMapPath.parent as? ConstraintLayout ?: return
        currentLevelMarker?.let { mapContainer.removeView(it) }

        val size = 22.dpToPx()
        val (cx, cy) = getLevelCenterPx(currentHeroLevel)

        currentLevelMarker = ImageView(requireContext()).apply {
            setImageResource(R.drawable.map_purple_circle)
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(size, size).apply {
                startToStart = binding.ivMapPath.id
                topToTop = binding.ivMapPath.id
                marginStart = (cx - size / 2).coerceAtLeast(0)
                topMargin = (cy - size / 2).coerceAtLeast(0)
            }

            // 등장 애니메이션
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
            animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(300).start()
        }

        mapContainer.addView(currentLevelMarker)
    }

    /**
     * "현재 위치" 라벨(보라) - 현재 점 오른쪽에 붙임
     */
    private fun setupLocationLabel() {
        val mapContainer = binding.ivMapPath.parent as? ConstraintLayout ?: return
        locationLabel?.let { mapContainer.removeView(it) }

        val marker = currentLevelMarker ?: return
        val (cx, cy) = getLevelCenterPx(currentHeroLevel)

        // 라벨 크기(원하면 dp 조절)
        val labelW = 70.dpToPx()
        val labelH = 26.dpToPx()
        val gap = 6.dpToPx()

        locationLabel = ImageView(requireContext()).apply {
            setImageResource(R.drawable.map_purple_location)
            id = View.generateViewId()

            layoutParams = ConstraintLayout.LayoutParams(labelW, labelH).apply {
                startToStart = binding.ivMapPath.id
                topToTop = binding.ivMapPath.id

                // 점 오른쪽에 배치
                marginStart = (cx + (marker.layoutParams.width / 2) + gap).coerceAtLeast(0)
                // 라벨 세로 중앙을 점 중앙에 맞추기
                topMargin = (cy - labelH / 2).coerceAtLeast(0)
            }

            alpha = 0f
            translationX = -10f
            animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay(120).start()
        }

        mapContainer.addView(locationLabel)
    }

    /**
     * 현재 레벨 점이 화면 중앙에 오도록 스크롤
     */
    private fun scrollToHeroLevel(animated: Boolean) {
        val (_, cy) = getLevelCenterPx(currentHeroLevel)

        val scrollView = binding.mapScrollView
        val screenH = scrollView.height

        scrollView.post {
            // NestedScrollView 내용 높이
            val content = scrollView.getChildAt(0)
            val maxScroll = max(0, content.height - scrollView.height)

            val target = (cy - screenH / 2).coerceIn(0, maxScroll)

            if (animated) {
                val from = scrollView.scrollY
                ValueAnimator.ofInt(from, target).apply {
                    duration = 650
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { scrollView.scrollTo(0, it.animatedValue as Int) }
                    start()
                }
            } else {
                scrollView.scrollTo(0, target)
            }
        }
    }

    /**
     * 레벨업 등 외부에서 호출하면 갱신 가능
     */
    fun updateHeroLevel(newLevel: Int) {
        val lv = newLevel.coerceIn(1, 100)
        if (lv == currentHeroLevel) return
        currentHeroLevel = lv
        setupMapUI()
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearedLevelMarkers.clear()
        currentLevelMarker = null
        locationLabel = null
        _binding = null
    }

    companion object {
        // index 0 unused
        // ※ 아래 값은 "점 중심" 기준 (map_path.png 기준). 만약에 안 되면 그냥 주석처리 후 map_path_1.png 파일 사용
        private val LEVEL_X_RATIO = floatArrayOf(
            0.000000f,
            0.391667f, 0.432286f, 0.460263f, 0.421765f, 0.574857f, 0.481818f, 0.431667f, 0.484167f, 0.287647f,
            0.109444f, 0.147838f, 0.188000f, 0.145000f, 0.103611f, 0.069167f, 0.039111f, 0.015926f, 0.069524f, 0.109444f,
            0.147727f, 0.178200f, 0.233333f, 0.288200f, 0.337895f, 0.388400f, 0.426000f, 0.471000f, 0.510000f, 0.548000f,
            0.570000f, 0.565000f, 0.535000f, 0.498000f, 0.452000f, 0.404000f, 0.353000f, 0.304000f, 0.255000f, 0.210000f,
            0.170000f, 0.130000f, 0.100000f, 0.070000f, 0.040000f, 0.010000f, 0.040000f, 0.080000f, 0.120000f, 0.160000f,
            0.200000f, 0.240000f, 0.280000f, 0.320000f, 0.360000f, 0.400000f, 0.440000f, 0.480000f, 0.520000f, 0.560000f,
            0.520000f, 0.480000f, 0.440000f, 0.400000f, 0.360000f, 0.320000f, 0.280000f, 0.240000f, 0.200000f, 0.160000f,
            0.120000f, 0.080000f, 0.040000f, 0.010000f, 0.040000f, 0.080000f, 0.120000f, 0.160000f, 0.200000f, 0.240000f,
            0.280000f, 0.320000f, 0.360000f, 0.400000f, 0.440000f, 0.480000f, 0.520000f, 0.560000f, 0.520000f, 0.480000f,
            0.440000f, 0.400000f, 0.360000f, 0.320000f, 0.280000f, 0.240000f, 0.200000f, 0.160000f, 0.120000f, 0.080000f,
            0.442400f
        )

        private val LEVEL_Y_RATIO = floatArrayOf(
            0.000000f,
            0.994640f, 0.989517f, 0.983949f, 0.949961f, 0.948278f, 0.959717f, 0.960316f, 0.965148f, 0.933851f,
            0.913767f, 0.919599f, 0.926452f, 0.921102f, 0.914297f, 0.908841f, 0.903206f, 0.897619f, 0.882093f, 0.877083f,
            0.871827f, 0.866906f, 0.852292f, 0.837778f, 0.823082f, 0.808731f, 0.795001f, 0.781232f, 0.767319f, 0.753589f,
            0.739893f, 0.726004f, 0.712177f, 0.698351f, 0.684496f, 0.670649f, 0.656767f, 0.642917f, 0.629060f, 0.615244f,
            0.601399f, 0.587559f, 0.573707f, 0.559872f, 0.546036f, 0.532194f, 0.518360f, 0.504526f, 0.490691f, 0.476862f,
            0.463029f, 0.449194f, 0.435361f, 0.421528f, 0.407695f, 0.393862f, 0.380029f, 0.366196f, 0.352363f, 0.338530f,
            0.324697f, 0.310864f, 0.297031f, 0.283198f, 0.269365f, 0.255532f, 0.241699f, 0.227866f, 0.214033f, 0.200200f,
            0.186367f, 0.172534f, 0.158701f, 0.144868f, 0.131035f, 0.117202f, 0.103369f, 0.089536f, 0.075703f, 0.061870f,
            0.048037f, 0.040847f, 0.046538f, 0.051448f, 0.056464f, 0.061494f, 0.066525f, 0.071555f, 0.076586f, 0.081616f,
            0.086647f, 0.091677f, 0.096708f, 0.101738f, 0.106769f, 0.111799f, 0.116830f, 0.121860f, 0.126891f, 0.131921f,
            0.011979f
        )
    }
}
