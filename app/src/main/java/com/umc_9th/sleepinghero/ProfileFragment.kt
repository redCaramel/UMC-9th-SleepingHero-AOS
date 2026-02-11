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
    private var equippedSkin = 1
    private val skinImgList = arrayOf(
        R.drawable.ic_hero_1,
        R.drawable.ic_hero_2,
        R.drawable.ic_hero_3,
        R.drawable.ic_hero_4,
        R.drawable.ic_hero_5,
        R.drawable.ic_hero_6,
        R.drawable.ic_hero_7,
        R.drawable.ic_hero_8,
        R.drawable.ic_hero_9,
        R.drawable.ic_hero_10,
        R.drawable.ic_hero_11,
        R.drawable.ic_hero_12
    )
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

            dialogBinding.btnHeroA.setOnClickListener {
                if(!skinList[0]) Toast.makeText(requireContext(), "모험을 진행하며 아바타를 해금하세요!", Toast.LENGTH_LONG).show()
                else if(equippedSkin >= 1 && equippedSkin <=3) Toast.makeText(requireContext(), "이미 착용중인 아바타입니다!", Toast.LENGTH_LONG).show()
                else {
                    socialViewModel.equipSKin(TokenManager.getAccessToken(requireContext()).toString(), 1)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SocialFragment())
                        .commit()
                }
            }
            dialogBinding.btnHeroB.setOnClickListener {
                if(!skinList[1]) Toast.makeText(requireContext(), "모험을 진행하며 아바타를 해금하세요!", Toast.LENGTH_LONG).show()
                else if(equippedSkin >= 4 && equippedSkin <=6) Toast.makeText(requireContext(), "이미 착용중인 아바타입니다!", Toast.LENGTH_LONG).show()
                else {
                    socialViewModel.equipSKin(TokenManager.getAccessToken(requireContext()).toString(), 4)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SocialFragment())
                        .commit()
                }
            }
            dialogBinding.btnHeroC.setOnClickListener {
                if(!skinList[2]) Toast.makeText(requireContext(), "모험을 진행하며 아바타를 해금하세요!", Toast.LENGTH_LONG).show()
                else if(equippedSkin >= 7 && equippedSkin <=9) Toast.makeText(requireContext(), "이미 착용중인 아바타입니다!", Toast.LENGTH_LONG).show()
                else {
                    socialViewModel.equipSKin(TokenManager.getAccessToken(requireContext()).toString(), 7)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SocialFragment())
                        .commit()
                }
            }
            dialogBinding.btnHeroD.setOnClickListener {
                if(!skinList[3]) Toast.makeText(requireContext(), "모험을 진행하며 아바타를 해금하세요!", Toast.LENGTH_LONG).show()
                else if(equippedSkin >= 10 && equippedSkin <=12) Toast.makeText(requireContext(), "이미 착용중인 아바타입니다!", Toast.LENGTH_LONG).show()
                else {
                    socialViewModel.equipSKin(TokenManager.getAccessToken(requireContext()).toString(), 10)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SocialFragment())
                        .commit()
                }
            }

            if(equippedSkin >= 1 && equippedSkin <=3) {
                dialogBinding.btnHeroA.text = "선택 중"
                dialogBinding.btnHeroA.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D6E2EF"))
            }
            if(equippedSkin >=4 && equippedSkin <=6) {
                dialogBinding.btnHeroB.text = "선택 중"
                dialogBinding.btnHeroB.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D6E2EF"))
            }
            if(equippedSkin >= 7 && equippedSkin <=9) {
                dialogBinding.btnHeroC.text = "선택 중"
                dialogBinding.btnHeroC.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D6E2EF"))
            }
            if(equippedSkin >=10 && equippedSkin <=12) {
                dialogBinding.btnHeroD.text = "선택 중"
                dialogBinding.btnHeroD.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D6E2EF"))
            }

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
                    if(skin.equipped) equippedSkin = skin.skinId.toInt()
                }
                binding.imgProfileHead.setImageResource(skinImgList[equippedSkin-1])
                binding.imgProfileBody.setImageResource(skinImgList[equippedSkin-1])
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "열람 실패 : $message")
            }
        }
        socialViewModel.equipSKinResponse.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "아바타를 변경하였습니다!", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "아바타를 변경할 수 없습니다!", Toast.LENGTH_SHORT).show()
                val message = error.message ?: "알 수 없는 오류"
                Log.d("test", "변경 실패 : $message")
            }
        }
    }
}