package com.umc_9th.sleepinghero

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemFriendRequestBinding
import okhttp3.internal.notify

class FriendRequestAdapter(private var heroList : MutableList<HeroData>,
                           private val acceptEvent: (HeroData) -> Unit,
                           private val rejectEvent: (HeroData) -> Unit
    ) : RecyclerView.Adapter<FriendRequestAdapter.HeroViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeroViewHolder {
       val binding = ItemFriendRequestBinding.inflate(
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
    inner class HeroViewHolder(val binding : ItemFriendRequestBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(hero : HeroData) {
                    binding.tvCharName.text = hero.name
                    binding.imgCharProfile.setImageResource(hero.skinId)
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
                    binding.imgCharProfile.setImageResource(skinImgList[hero.skinId-1])
                    binding.btnAccept.setOnClickListener {
                        acceptEvent(hero)
                    }
                    binding.btnReject.setOnClickListener {
                        rejectEvent(hero)
                    }
                }
            }
}
