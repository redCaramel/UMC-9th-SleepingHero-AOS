package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.ActivityAvaterBinding
import com.umc_9th.sleepinghero.databinding.ActivityCreateGroupBinding
import com.umc_9th.sleepinghero.databinding.FragmentProfileBinding
import kotlin.getValue

class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
    lateinit var mainActivity: MainActivity
    private val socialRepository by lazy {
        SocialRepository(ApiClient.socialService)
    }
    private var skinList = arrayOf(true, false, false, false)
    private val socialViewModel : SocialViewModel by viewModels(
        factoryProducer = { SocialViewModelFactory(socialRepository) }
    )
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        observeSocial()
        socialViewModel.checkSkin(TokenManager.getAccessToken(requireContext()).toString())
        socialViewModel.myCharacter(TokenManager.getAccessToken(requireContext()).toString())
        binding.imgProfileHead.setOnClickListener {
            val dialogBinding = ActivityAvaterBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
                .setView(dialogBinding.root)
                .setTitle("보유 아바타")
                .create()
            var skinNum = 4
            if(!skinList[1]) {
                dialogBinding.containerHeroB.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#c9c9c9"))
                dialogBinding.textView39.setTextColor(Color.parseColor("#828282"))
                dialogBinding.textView41.setTextColor(Color.parseColor("#828282"))
                dialogBinding.btnHeroB.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#828282"))
                dialogBinding.btnHeroB.setText("잠금")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blurEffect = RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
                    dialogBinding.imgHeroB.setRenderEffect(blurEffect)
                }
                skinNum--
            }
            if(!skinList[2]) {
                dialogBinding.containerHeroC.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#c9c9c9"))
                dialogBinding.textView42.setTextColor(Color.parseColor("#828282"))
                dialogBinding.textView43.setTextColor(Color.parseColor("#828282"))
                dialogBinding.btnHeroC.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#828282"))
                dialogBinding.btnHeroC.setText("잠금")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blurEffect = RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
                    dialogBinding.imgHeroC.setRenderEffect(blurEffect)
                }
                skinNum--
            }
            if(!skinList[3]) {
                dialogBinding.containerHeroD.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#c9c9c9"))
                dialogBinding.textView44.setTextColor(Color.parseColor("#828282"))
                dialogBinding.textView45.setTextColor(Color.parseColor("#828282"))
                dialogBinding.btnHeroD.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#828282"))
                dialogBinding.btnHeroD.setText("잠금")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blurEffect = RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
                    dialogBinding.imgHeroD.setRenderEffect(blurEffect)
                }
                skinNum--
            }
            dialogBinding.progressBar.setProgress(skinNum)
            dialogBinding.tvCharacterNum.text = "$skinNum / 4"
            dialog.show()
        }
        binding.btnProfileBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        binding.btnProfileCancel.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        binding.etNicknameSetting.addTextChangedListener {
            binding.tvPreviewNickname.text = binding.etNicknameSetting.text

        }
        binding.btnProfileConfirm.setOnClickListener {
            socialViewModel.changeName(TokenManager.getAccessToken(requireContext()).toString(),
                binding.etNicknameSetting.text.toString().trim()
            )
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, SocialFragment())
                .commit()
        }
        return binding.root
    }
    private fun observeSocial() {
        socialViewModel.myCharResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.etNicknameSetting.setText(data.name)
                binding.tvPreviewNickname.text = data.name
                binding.tvPreviewLevel.text = "Lv.${data.currentLevel}"
                binding.tvInfoLevel.text = "Lv.${data.currentLevel}"
                socialViewModel.charSearch(TokenManager.getAccessToken(requireContext()).toString(), data.name)
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")

            }
        }
        socialViewModel.changeNameResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "적용 성공!", Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")
            }
        }
        socialViewModel.charSearchResponse.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data ->
                binding.tvPreviewStreak.text = "${data.continuousSleepDays}일"
                binding.tvPreviewHour.text = "${data.totalSleepHour}시간"
                binding.tvInfoStreak.text = "${data.continuousSleepDays}일"
                binding.tvInfoHour.text = "${data.totalSleepHour}시간"
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "불러오기 실패 : $message")
            }
        }
        socialViewModel.checkSkinResponse.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data ->
                data.skins.forEach { skin ->
                    if(skin.skinId==1.toLong()) skinList[0] = true
                    if(skin.skinId==4.toLong()) skinList[1] = true
                    if(skin.skinId==7.toLong()) skinList[2] = true
                    if(skin.skinId==10.toLong()) skinList[3] = true
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "열람 실패 : $message")
            }
        }
    }
}