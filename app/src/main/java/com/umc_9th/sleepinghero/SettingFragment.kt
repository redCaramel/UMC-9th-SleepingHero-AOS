package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding
import com.umc_9th.sleepinghero.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private lateinit var binding : FragmentSettingBinding
    lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
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
                dialog.dismiss()
            }
            dialogBinding.btnTimesetCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

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
}