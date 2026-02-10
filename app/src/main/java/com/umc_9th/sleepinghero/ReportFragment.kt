package com.umc_9th.sleepinghero

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import com.umc_9th.sleepinghero.api.viewmodel.SettingViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SettingViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentReportBinding
import kotlin.getValue

class ReportFragment : Fragment() {

    private lateinit var binding : FragmentReportBinding
    private val settingRepository by lazy {
        SettingRepository(ApiClient.settingService)
    }
    private val settingViewModel : SettingViewModel by viewModels(
        factoryProducer = { SettingViewModelFactory(settingRepository) }
    )
    private lateinit var menuList : List<View>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        observeFAQ()
        menuList = listOf(
            binding.selectorBug,
            binding.selectorReport
        )
        menuList.forEach { menu ->
            menu.setOnClickListener {
                updateSelection(menu)
            }
        }
        binding.btnReportBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SettingFragment())
                .commit()

        }
        binding.btnReport.setOnClickListener {
            if(binding.etReport.text.length >= 1) {
                var type : String = "BUG"
                if(binding.selectorReport.isSelected) type = "REPORT"
                settingViewModel.FAQUrl(TokenManager.getAccessToken(requireContext()).toString(), type, binding.etReport.text.toString(), binding.etReportEmail.text.toString())
            }
        }
        updateSelection(binding.selectorBug)
        return binding.root
    }
    private fun updateSelection(selectedMenu: View) {
        menuList.forEach { it.isSelected = false }
        selectedMenu.isSelected = true
        if(selectedMenu == binding.selectorBug) {
            binding.selectorBug.setTextColor(Color.parseColor("#FFFFFF"))
            binding.selectorReport.setTextColor(Color.parseColor("#000000"))
            binding.tvReportDescription.text = "앱 사용 중 발생한 오류나 버그를 알려주세요.\n정확한 상황 설명은 빠른 해결에 도움이 됩니다."
            binding.etReport.hint = "예) 수면 시작 버튼을 눌렀는데 반응이 없어요.\n- 발생 일시 : \n- 기기 모델 :"
        }
        else {
            binding.selectorReport.setTextColor(Color.parseColor("#FFFFFF"))
            binding.selectorBug.setTextColor(Color.parseColor("#000000"))
            binding.tvReportDescription.text = "더 나은 서비스를 위한 아이디어나 불편했던 점을 자유롭게 말씀해주세요."
            binding.etReport.hint = "예) 친구들과 함께하는 챌린지 기능이 있었으면 좋겠어요."
        }
    }
    private fun observeFAQ() {
        settingViewModel.faqUrlResult.observe(viewLifecycleOwner) { result ->
            //Result -> status, code 등이 있고 이 안 data에 값이 존재
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "전송에 성공했습니다.", Toast.LENGTH_LONG).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container_main, SettingFragment())
                    .commit()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "연결 실패: $message")
                Toast.makeText(requireContext(),"전송에 실패했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }
}