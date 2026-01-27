package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc_9th.sleepinghero.databinding.ActivityCreateGroupBinding
import com.umc_9th.sleepinghero.databinding.FragmentGroupBinding

class GroupFragment : Fragment() {
    private lateinit var binding: FragmentGroupBinding
    lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        binding.btnCreateGroup.setOnClickListener {
            val dialogBinding = ActivityCreateGroupBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
                .setView(dialogBinding.root)
                .setTitle("새 그룹 만들기")
                .create()
            dialogBinding.btnCreateGroupCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.btnCreateGroupConfirm.setOnClickListener {
                // TODO - 그룹 생성 구현
                dialog.dismiss()
            }
            dialog.show()
        }
        return binding.root
    }
}