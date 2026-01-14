package com.umc_9th.sleepinghero

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.umc_9th.sleepinghero.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val keyHash = com.kakao.sdk.common.util.Utility.getKeyHash(this)
        Log.d("KeyHash", keyHash)
        binding.btnLoginNaver.setOnClickListener {
            // TODO - 네이버 로그인 구현
        }
        binding.btnLoginKakao.setOnClickListener {
            kakaoLogin()
        }
    }

    private fun kakaoLogin() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.d("kakaoerror", error.toString())
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부되었습니다 (동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "기타 에러가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (token != null) {
                Log.d("KakaoLogin", "카카오 로그인 성공: ${token.accessToken}")
                // TODO - 카카오 로그인 후 토큰 처리
                accessService(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun accessService(token: String) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("token", token)
        startActivity(intent)
        finish()
    }
}