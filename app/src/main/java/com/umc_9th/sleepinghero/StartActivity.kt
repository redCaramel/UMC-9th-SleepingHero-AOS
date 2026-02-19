package com.umc_9th.sleepinghero

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.umc_9th.sleepinghero.databinding.ActivityStartBinding
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.AuthRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.repository.TutorialRepository
import com.umc_9th.sleepinghero.api.viewmodel.AuthViewModel
import com.umc_9th.sleepinghero.api.viewmodel.AuthViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.TutorialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.TutorialViewModelFactory

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private val authyRepository by lazy {
        AuthRepository(ApiClient.authService)
    }
    private val authViewModel: AuthViewModel by viewModels(
        factoryProducer = { AuthViewModelFactory(authyRepository) }
    )
    private val socialRepository by lazy {
        SocialRepository(ApiClient.socialService)
    }
    private val socialViewModel : SocialViewModel by viewModels(
        factoryProducer = { SocialViewModelFactory(socialRepository) }
    )

    private val tutorialRepository by lazy {
        TutorialRepository(ApiClient.tutorialService)
    }

    private val tutorialViewModel: TutorialViewModel by viewModels(
        factoryProducer = { TutorialViewModelFactory(tutorialRepository) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeLogin()
        observeCheck()


        checkLogin()
        binding.btnLoginNaver.setOnClickListener {
            NaverIdLoginSDK.authenticate(this, naverLoginCallback)
        }
        binding.btnLoginKakao.setOnClickListener {
            kakaoLogin()
        }
    }

    private fun checkLogin() {
        if(TokenManager.isLoggedin(this)) {
            accessService(TokenManager.getAccessToken(this).toString())
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
                authViewModel.kakaoLogin(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }
    val naverLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            Log.d("test", "AccessToken : " + NaverIdLoginSDK.getAccessToken())
            Log.d("test", "ReFreshToken : " + NaverIdLoginSDK.getRefreshToken())
            Log.d("test", "Expires : " + NaverIdLoginSDK.getExpiresAt().toString())
            Log.d("test", "TokenType : " + NaverIdLoginSDK.getTokenType())
            Log.d("test", "State : " + NaverIdLoginSDK.getState().toString())
            authViewModel.naverLogin(NaverIdLoginSDK.getAccessToken() as String)
        }
        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            Log.e("test", "$errorCode $errorDescription")
        }
        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }
    private fun accessService(token: String) {
        TokenManager.setAccessToken(this, token)
        socialViewModel.myCharacter(token)

    }

    private fun observeLogin() {
        authViewModel.kakaoLoginResult.observe(this) { result ->
            //Result -> status, code 등이 있고 이 안 data에 값이 존재
            result.onSuccess { data ->
                accessService(data.accessToken)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류가 발생했습니다."
                Log.d("tag", "로그인 실패: $message")
                Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_LONG).show()
            }
        }
        authViewModel.naverLoginResult.observe(this) { result ->
            //Result -> status, code 등이 있고 이 안 data에 값이 존재
            result.onSuccess { data ->
                accessService(data.accessToken)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류가 발생했습니다."
                Log.d("tag", "로그인 실패: $message")
                Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun observeCheck() {
        socialViewModel.myCharResponse.observe(this) { result ->
            result.onSuccess { data ->
                val token = TokenManager.getAccessToken(this).orEmpty()
                tutorialViewModel.getTutorial(token)
            }.onFailure { error ->
                if (error.message?.contains("존재하지 않는 캐릭터입니다.") == true) {
                    startActivity(Intent(this, CreateHeroActivity::class.java))
                    finish()
                }
            }
        }

        tutorialViewModel.tutorialStatus.observe(this) { result ->
            result.onSuccess { status ->
                if (!status.finished) {
                    startActivity(Intent(this, TutorialActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }.onFailure {
                startActivity(Intent(this, TutorialActivity::class.java))
                finish()
            }
        }
    }

}