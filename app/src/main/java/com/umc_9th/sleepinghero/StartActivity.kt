package com.umc_9th.sleepinghero

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.umc_9th.sleepinghero.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvStart.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_login, null)
            val mBuilder = AlertDialog.Builder(this, R.style.PopupAnimStyle)
                .setView(mDialogView)
                .setTitle("로그인")
            mBuilder.show()
        }
    }
}