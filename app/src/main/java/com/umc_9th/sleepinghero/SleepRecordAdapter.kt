package com.umc_9th.sleepinghero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SleepRecordUiModel(
    val date: String,
    val sleepTimeText: String,
    val star: Int,
    val advice: String
)

class SleepRecordAdapter : RecyclerView.Adapter<SleepRecordAdapter.SleepRecordViewHolder>() {

    private val items = mutableListOf<SleepRecordUiModel>()

    fun submitList(newItems: List<SleepRecordUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class SleepRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvSleepTime: TextView = itemView.findViewById(R.id.tvSleepTime)
        val ivStar: ImageView = itemView.findViewById(R.id.ivStar)
        val tvAdvice: TextView = itemView.findViewById(R.id.tvAdvice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sleep_record, parent, false)
        return SleepRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: SleepRecordViewHolder, position: Int) {
        val item = items[position]

        holder.tvDate.text = item.date
        holder.tvSleepTime.text = item.sleepTimeText
        holder.tvAdvice.text = item.advice
        holder.ivStar.setImageResource(getStarDrawable(item.star))
    }

    private fun getStarDrawable(star: Int): Int {
        return when (star) {
            1 -> R.drawable.ic_star_1
            2 -> R.drawable.ic_star_2
            3 -> R.drawable.ic_star_3
            4 -> R.drawable.ic_star_4
            5 -> R.drawable.ic_star_5
            else -> R.drawable.ic_star_1
        }
    }

    override fun getItemCount(): Int = items.size
}

