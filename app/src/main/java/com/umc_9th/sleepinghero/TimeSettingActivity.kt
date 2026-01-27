package com.umc_9th.sleepinghero

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding

class TimeSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTimeSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}