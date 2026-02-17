package com.umc_9th.sleepinghero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.umc_9th.sleepinghero.databinding.FragmentSleepStopBinding

class SleepStopFragment : DialogFragment() {

    private var _binding: FragmentSleepStopBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 배경 투명 + XML에서 dim 처리(#80000000) 하니까 여기선 기본 다이얼로그 타이틀 제거만
        setStyle(STYLE_NO_TITLE, R.style.SleepStopDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepStopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바깥 터치로 닫히는 거 원하면 true 유지, 싫으면 false로
        isCancelable = true

        // "계속" 버튼
        binding.btnResume.setOnClickListener {
            setFragmentResult(
                REQ_KEY,
                Bundle().apply { putString("action", ACTION_RESUME) }
            )
            dismiss()
        }

        // "중단" 버튼
        binding.btnStop.setOnClickListener {
            setFragmentResult(
                REQ_KEY,
                Bundle().apply { putString("action", ACTION_STOP) }
            )
            dismiss()
        }

        // 배경(오버레이) 클릭 시 닫기 원하면 아래 추가 (카드 밖 클릭 영역이 root 전체일 때만)
        binding.sleepStopRoot.setOnClickListener {
            dismiss()
        }

        // 카드 영역 클릭은 이벤트 먹게(배경 클릭으로 dismiss 되는 것 방지)
        binding.sleepStopCard.setOnClickListener {
            // no-op
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQ_KEY = "sleep_stop_dialog_result"
        const val ACTION_RESUME = "resume"
        const val ACTION_STOP = "stop"
    }
}