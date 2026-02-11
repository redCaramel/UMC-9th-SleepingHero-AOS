package com.umc_9th.sleepinghero

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.umc_9th.sleepinghero.databinding.ActivityAvaterBinding

class AvaterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvaterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAvaterBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}