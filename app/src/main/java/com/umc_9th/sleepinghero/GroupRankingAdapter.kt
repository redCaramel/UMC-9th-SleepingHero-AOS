package com.umc_9th.sleepinghero

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemGroupRankingBinding
import com.umc_9th.sleepinghero.databinding.ItemRankingBinding
import okhttp3.internal.notify

class GroupRankingAdapter(private var heroList : MutableList<GroupRankingData>
    ) : RecyclerView.Adapter<GroupRankingAdapter.HeroViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeroViewHolder {
       val binding = ItemGroupRankingBinding.inflate(
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

    fun updateList(newList : List<GroupRankingData>) {
        heroList.clear()
        heroList.addAll(newList)
        notifyDataSetChanged()
    }
    fun uploadList(newList : GroupRankingData) {
        this.heroList.add(newList)
        notifyItemInserted(heroList.size-1)
    }
    fun removeItem(target : GroupRankingData) {
        val pos = heroList.indexOfFirst { it.name == target.name }
        if(pos != -1) {
            heroList.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, heroList.size)
        }
    }
    inner class HeroViewHolder(val binding : ItemGroupRankingBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(hero : GroupRankingData) {
                    binding.tvCharName.text = hero.name
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
                    binding.tvGroupRole.text = hero.role
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
                }
            }
}
