package com.umc_9th.sleepinghero

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.CreateRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.CreateViewModel
import com.umc_9th.sleepinghero.api.viewmodel.CreateViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.ActivityCreateHeroBinding
import kotlin.getValue

class CreateHeroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateHeroBinding
    private val createRepository by lazy {
        CreateRepository(ApiClient.createService)
    }
    private val createViewModel : CreateViewModel by viewModels(
        factoryProducer = { CreateViewModelFactory(createRepository) }
    )
    private val changeRepository by lazy {
        SocialRepository(ApiClient.socialService)
    }
    private val changeViewModel : SocialViewModel by viewModels(
        factoryProducer = { SocialViewModelFactory(changeRepository) }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        observeCreate()
        binding = ActivityCreateHeroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnCreateHero.setOnClickListener {
            createViewModel.CreateHero(TokenManager.getAccessToken(this).toString())
            changeViewModel.changeName(TokenManager.getAccessToken(this).toString(), binding.etHeroName.text.toString())
        }
    }
    private fun observeCreate() {
        createViewModel.heroCreateResult.observe(this) { result ->
            result.onSuccess { data ->
                Log.d("test", "사용자 생성 성공 - ${data.name}")
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "생성 실패 : $message")
            }
        }
        changeViewModel.changeNameResponse.observe(this) { result ->
            result.onSuccess { data ->
                Log.d("test", "사용자 이름 설정 성공 - ${data.name}")
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}