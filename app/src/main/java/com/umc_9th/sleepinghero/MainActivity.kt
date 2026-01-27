package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.umc_9th.sleepinghero.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container_main, HomeFragment())
            .commit()

        binding.navMain.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.Home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, HomeFragment())
                        .commit()
                    true
                }

                R.id.Settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SettingFragment())
                        .commit()
                    true
                }

                 R.id.Social -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SocialFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }
}