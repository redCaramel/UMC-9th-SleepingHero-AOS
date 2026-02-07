package com.umc_9th.sleepinghero

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemFriendSearchBinding
import okhttp3.internal.notify

class HeroAdapter(private var heroList : MutableList<HeroData>,
    private val onInvited: (HeroData) -> Unit
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
                    binding.imgCharProfile.setImageResource(hero.skinId)
                    binding.tvCharLevel.text = "Lv. ${hero.level}"
                    binding.tvCharStreak.text = "${hero.streak}일 연속 달성"
                    binding.tvCharTotal.text = "${hero.total}시간"
                    binding.btnFriendInvite.setOnClickListener {
                        onInvited(hero)
                    }
                }
            }
}
