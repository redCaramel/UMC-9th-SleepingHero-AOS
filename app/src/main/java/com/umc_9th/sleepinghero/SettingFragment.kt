package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SettingRepository
import com.umc_9th.sleepinghero.api.viewmodel.SettingViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SettingViewModelFactory
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding
import com.umc_9th.sleepinghero.databinding.FragmentSettingBinding
import androidx.core.net.toUri

class SettingFragment : Fragment() {
    private lateinit var binding : FragmentSettingBinding
    lateinit var mainActivity: MainActivity
    lateinit var settingManager: SettingManager
    private val settingRepository by lazy {
        SettingRepository(ApiClient.settingService)
    }
    private val settingViewModel : SettingViewModel by viewModels(
        factoryProducer = { SettingViewModelFactory(settingRepository) }
    )
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        observeFAQ()
        settingManager = SettingManager(requireContext())
        return binding.root
    }

    private fun initSettingView() {
        binding.swPushalarm.isChecked = settingManager.getPushAlarm()
        binding.swNodistract.isChecked = settingManager.getNoDisturb()
        if(settingManager.getNoDisturb()) binding.cardNodistractSetting.visibility = View.VISIBLE
        if(settingManager.getAwakeTime() == "null") settingManager.setAwakeTime("07:00 AM")
        binding.tvTimeAwake.text = settingManager.getAwakeTime()
        if(settingManager.getSleepTime() == "null") settingManager.setSleepTime("11:00 PM")
        binding.tvTimeSleep.text = settingManager.getSleepTime()
        binding.sbAlarmVolume.progress = settingManager.getAlarmVolume()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        initSettingView()
        binding.swPushalarm.setOnCheckedChangeListener { CompoundButton, onSwitch ->
            settingManager.setPushAlarm(onSwitch)
        }
        binding.swNodistract.setOnCheckedChangeListener { CompoundButton, onSwitch ->
            settingManager.setNoDisturb(onSwitch)
            if(onSwitch) {
                binding.cardNodistractSetting.visibility=View.VISIBLE
            }
            else {
                binding.cardNodistractSetting.visibility=View.GONE
            }
        }
        binding.layoutSleep.setOnClickListener {
            val dialogBinding = ActivityTimeSettingBinding.inflate(layoutInflater)

            val time = parseTimeString(binding.tvTimeSleep.text.toString())
            var hour = time.first
            var min = time.second
            var ampm = time.third

            dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
            dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
            dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)

            val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
                .setView(dialogBinding.root)
                .setTitle("취침 시간 설정")
                .create()


            dialogBinding.btnTimesetHourup.setOnClickListener {
                if (hour == 11) {
                    hour = 12
                    ampm = 1 - ampm
                } else if (hour == 12) {
                    hour = 1
                } else {
                    hour++
                }
                dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }

            dialogBinding.btnTimesetHourdown.setOnClickListener {
                if (hour == 12) {
                    hour = 11
                } else if (hour == 1) {
                    hour = 12
                    ampm = 1 - ampm
                } else {
                    hour--
                }
                dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }

            dialogBinding.btnTimesetMinup.setOnClickListener {
                min = if (min == 50) 0 else min + 10
                dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
            }

            dialogBinding.btnTimesetMindown.setOnClickListener {
                min = if (min == 0) 50 else min - 10
                dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
            }

            dialogBinding.btnTimesetAmpmA.setOnClickListener {
                ampm = 1 - ampm
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }
            dialogBinding.btnTimesetAmpmB.setOnClickListener {
                ampm = 1 - ampm
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }

            dialogBinding.btnTimesetConfirm.setOnClickListener {
                val finalStr = "${makeTimeString(hour, 0)}:${makeTimeString(min, 0)} ${makeTimeString(ampm, 1)}"
                binding.tvTimeSleep.text = finalStr
                settingManager.setSleepTime(finalStr)
                dialog.dismiss()
            }
            dialogBinding.btnTimesetCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        binding.layoutAwake.setOnClickListener {
            val dialogBinding = ActivityTimeSettingBinding.inflate(layoutInflater)

            val time = parseTimeString(binding.tvTimeAwake.text.toString())
            var hour = time.first
            var min = time.second
            var ampm = time.third

            dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
            dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
            dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)

            val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
                .setView(dialogBinding.root)
                .setTitle("기상 시간 설정")
                .create()


            dialogBinding.btnTimesetHourup.setOnClickListener {
                if (hour == 11) {
                    hour = 12
                    ampm = 1 - ampm
                } else if (hour == 12) {
                    hour = 1
                } else {
                    hour++
                }
                dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }

            dialogBinding.btnTimesetHourdown.setOnClickListener {
                if (hour == 12) {
                    hour = 11
                } else if (hour == 1) {
                    hour = 12
                    ampm = 1 - ampm
                } else {
                    hour--
                }
                dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }

            dialogBinding.btnTimesetMinup.setOnClickListener {
                min = if (min == 50) 0 else min + 10
                dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
            }

            dialogBinding.btnTimesetMindown.setOnClickListener {
                min = if (min == 0) 50 else min - 10
                dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
            }

            dialogBinding.btnTimesetAmpmA.setOnClickListener {
                ampm = 1 - ampm
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }
            dialogBinding.btnTimesetAmpmB.setOnClickListener {
                ampm = 1 - ampm
                dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
            }

            dialogBinding.btnTimesetConfirm.setOnClickListener {
                val finalStr = "${makeTimeString(hour, 0)}:${makeTimeString(min, 0)} ${makeTimeString(ampm, 1)}"
                binding.tvTimeAwake.text = finalStr
                settingManager.setAwakeTime(finalStr)
                dialog.dismiss()
            }
            dialogBinding.btnTimesetCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.sbAlarmVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                settingManager.setAlarmVolume(progress)
                binding.tvAlarmVolume.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.btnLogout.setOnClickListener {
            TokenManager.clearAll(requireContext())

            //kakao
            UserApiClient.instance.logout { error ->
                if(error != null) {
                    Log.e("test", "로그아웃 실패, SDK에서 토큰 폐기됨", error)
                }
                else {
                    Log.i("test", "로그아웃 성공")
                }
            }
            //naver
            NaverIdLoginSDK.logout()
            var intent = Intent(requireContext(), StartActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        binding.settingBugReport.setOnClickListener {
            settingViewModel.FAQUrl(TokenManager.getAccessToken(requireContext()).toString())
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

        if(settingManager.getAlarmType() == 0) {
            selectTab(binding.tabUnuse)
        }
        else if(settingManager.getAlarmType()==1) {
            selectTab(binding.tabSound)
            binding.cardAlarmVolume.visibility = View.VISIBLE
            binding.tvAlarmVolume.text = "${settingManager.getAlarmVolume()}%"
        }
        else {
            selectTab(binding.tabVibrate)
        }
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
                if(layout == binding.tabUnuse) {
                    settingManager.setAlarmType(0)
                }
                else if(layout == binding.tabSound) {
                    settingManager.setAlarmType(1)
                    binding.cardAlarmVolume.visibility = View.VISIBLE
                    binding.tvAlarmVolume.text = "${settingManager.getAlarmVolume()}%"
                }
                else if(layout == binding.tabVibrate) {
                    settingManager.setAlarmType(2)
                }
                if(layout != binding.tabSound) binding.cardAlarmVolume.visibility = View.GONE
            } else {
                layout.setBackgroundResource(R.drawable.tab_unselected)
                text.setTextColor(Color.parseColor("#666666"))
            }
        }
    }
    fun parseTimeString(timeStr: String): Triple<Int, Int, Int> {
        val parts = timeStr.split(" ")

        val time = parts[0]
        val ampm = parts[1]

        val (hourStr, minuteStr) = time.split(":")

        val hour = hourStr.toInt()
        val minute = minuteStr.toInt()
        val ampmFlag = if (ampm.equals("PM", ignoreCase = true)) 1 else 0

        return Triple(hour, minute, ampmFlag)
    }
    fun makeTimeString(time: Int, type: Int) : String{
        if(type == 1) {
            if(time==0) return "AM"
            else return "PM"
        }
        else {
            if(time < 10) return "0$time"
            else return time.toString()
        }
    }

    private fun observeFAQ() {
        settingViewModel.faqUrlResult.observe(viewLifecycleOwner) { result ->
            //Result -> status, code 등이 있고 이 안 data에 값이 존재
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "외부 링크로 연결합니다...", Toast.LENGTH_LONG).show()
                val faqIntent = Intent(Intent.ACTION_VIEW, data.inquiryUrl.toUri())
                startActivity(faqIntent)
                Log.d("test", "외부 링크 연결 - ${data.inquiryUrl}")
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "연결 실패: $message")
                Toast.makeText(requireContext(),"외부 링크 연결에 실패했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }
}