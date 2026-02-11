package com.umc_9th.sleepinghero

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.parser.ColorParser
import com.umc_9th.sleepinghero.databinding.ItemFriendSearchBinding
import okhttp3.internal.notify

class HeroAdapter(private var heroList : MutableList<HeroData>,
    private val clickEvent: (HeroData) -> Unit,
    private val type : Int
    ) : RecyclerView.Adapter<HeroAdapter.HeroViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeroViewHolder {
       val binding = ItemFriendSearchBinding.inflate(
           LayoutInflater.from(parent.context),
           parent, false)
        return HeroViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HeroViewHolder,
        position: Int
    ) {
        holder.bind(heroList[position])
    }

    override fun getItemCount(): Int {
        return heroList.size
    }

    fun updateList(newList : List<HeroData>) {
        heroList.clear()
        heroList.addAll(newList)
        notifyDataSetChanged()
    }
    inner class HeroViewHolder(val binding : ItemFriendSearchBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(hero : HeroData) {
                    binding.tvCharName.text = hero.name
                    binding.tvCharLevel.text = "Lv. ${hero.level}"
                    binding.tvCharStreak.text = "${hero.streak}일 연속 달성"
                    binding.tvCharTotal.text = "${hero.total}시간"
                    val skinImgList = arrayOf(
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
                    Log.d("test", "${hero.skinId}")
                    binding.imgCharProfile.setImageResource(skinImgList[hero.skinId-1])
                    binding.btnFriendInvite.setOnClickListener {
                        clickEvent(hero)
                    }
                    if(type == 2) {
                        binding.btnFriendInvite.setText("삭제")
                        binding.btnFriendInvite.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.btnFriendInvite.setBackgroundColor(Color.parseColor("#CF2323"))
                        binding.btnFriendInvite.icon = null
                    }
                }
            }
}
