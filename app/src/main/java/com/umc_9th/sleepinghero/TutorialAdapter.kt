package com.umc_9th.sleepinghero

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemTutorialBinding

class TutorialAdapter(
    private val items: List<TutorialPage>
) : RecyclerView.Adapter<TutorialAdapter.VH>() {

    inner class VH(val binding: ItemTutorialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTutorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.ivImage.setImageResource(item.imageRes)
        holder.binding.tvText.text = item.text
    }

    override fun getItemCount() = items.size
}
