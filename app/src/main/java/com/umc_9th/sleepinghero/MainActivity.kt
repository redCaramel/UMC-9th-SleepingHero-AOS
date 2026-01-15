package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.umc_9th.sleepinghero.databinding.ActivityMainBinding
import com.umc_9th.sleepinghero.ui.hero.HeroFragment

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

                R.id.Hero -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, HeroFragment())
                        .commit()
                    true
                }

                R.id.Routine -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, RoutineFragment())
                        .commit()
                    true
                }

                else -> false


                //TODO - 자기 파트 화면전환 추가
            }
        }
    }
}