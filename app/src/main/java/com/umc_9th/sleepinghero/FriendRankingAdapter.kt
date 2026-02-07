package com.umc_9th.sleepinghero

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemRankingBinding
import okhttp3.internal.notify

class FriendRankingAdapter(private var heroList : MutableList<FriendRankingData>
    ) : RecyclerView.Adapter<FriendRankingAdapter.HeroViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeroViewHolder {
       val binding = ItemRankingBinding.inflate(
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

    fun updateList(newList : List<FriendRankingData>) {
        heroList.clear()
        heroList.addAll(newList)
        notifyDataSetChanged()
    }
    inner class HeroViewHolder(val binding : ItemRankingBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(hero : FriendRankingData) {
                    binding.tvCharName.text = hero.nickName
                    binding.imgCharProfile.setImageResource(hero.skinId)
                    binding.tvCharLevel.text = "Lv. ${hero.level}"
                    binding.tvCharStreak.text = "${hero.streak}일 연속 달성"
                    binding.tvCharTotal.text = "${hero.total}시간"
                    binding.tvCharRank.text = "${hero.rank}"
                    if(hero.rank == 1) {
                        binding.tvCharRank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#f0b100"))
                    }
                    else if(hero.rank == 2) {
                        binding.tvCharRank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#b0b7c3"))
                    }
                    else if(hero.rank == 3) {
                        binding.tvCharRank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#af9d2c"))
                    }
                    else {
                        binding.tvCharRank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#575757"))
                    }
                }
            }
}
