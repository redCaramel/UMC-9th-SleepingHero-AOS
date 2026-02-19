package com.umc_9th.sleepinghero

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class AlarmDialogFragment : DialogFragment() {

    private var player: MediaPlayer? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val ctx = requireContext()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(ctx).apply {
            text = "기상 알람"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val sub = TextView(ctx).apply {
            text = "알람이 울리고 있습니다. 끄기 버튼을 누르세요."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val btn = Button(ctx).apply {
            text = "알람 끄기"
            setOnClickListener {
                stopAlarm()
                parentFragmentManager.setFragmentResult("ALARM_DISMISSED", Bundle.EMPTY)
                dismissAllowingStateLoss()
            }
        }

        root.addView(title)
        root.addView(sub)
        root.addView(btn)

        // ✅ 핵심: 소리 재생 여부
        val playSound = arguments?.getBoolean(ARG_PLAY_SOUND, false) ?: false
        if (playSound) startAlarm() // ON일 때만 소리

        return android.app.AlertDialog.Builder(ctx)
            .setView(root)
            .setCancelable(false)
            .create()
    }

    private fun startAlarm() {
        val alarmUri: Uri =
            Settings.System.DEFAULT_ALARM_ALERT_URI ?: Settings.System.DEFAULT_NOTIFICATION_URI

        try {
            player = MediaPlayer().apply {
                setDataSource(requireContext(), alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {}
    }

    private fun stopAlarm() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    companion object {
        private const val ARG_PLAY_SOUND = "arg_play_sound"

        fun newInstance(playSound: Boolean): AlarmDialogFragment {
            return AlarmDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PLAY_SOUND, playSound)
                }
            }
        }
    }
}
