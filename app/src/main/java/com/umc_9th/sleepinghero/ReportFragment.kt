package com.umc_9th.sleepinghero

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc_9th.sleepinghero.databinding.FragmentReportBinding

class ReportFragment : Fragment() {

    private lateinit var binding : FragmentReportBinding
    private lateinit var menuList : List<View>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater, container, false)

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
}