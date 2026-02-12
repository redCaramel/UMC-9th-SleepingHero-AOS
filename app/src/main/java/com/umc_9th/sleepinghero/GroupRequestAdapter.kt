package com.umc_9th.sleepinghero

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemFriendRequestBinding
import com.umc_9th.sleepinghero.databinding.ItemGroupRequestBinding

class GroupRequestAdapter(private var groupList : MutableList<GroupData>,
                          private val acceptEvent: (GroupData) -> Unit,
                          private val rejectEvent: (GroupData) -> Unit
    ) : RecyclerView.Adapter<GroupRequestAdapter.GroupViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupViewHolder {
       val binding = ItemGroupRequestBinding.inflate(
           LayoutInflater.from(parent.context),
           parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: GroupViewHolder,
        position: Int
    ) {
        holder.bind(groupList[position])
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    fun updateList(newList : GroupData) {
        this.groupList.add(newList)
        notifyItemInserted(groupList.size-1)
    }
    fun uploadList(newList : GroupData) {
        this.groupList.add(newList)
        notifyItemInserted(groupList.size-1)
    }
    fun removeItem(target : GroupData) {
        val pos = groupList.indexOfFirst { it.groupName == target.groupName }
        if(pos != -1) {
            groupList.removeAt(pos)
            notifyItemRemoved(pos)

            notifyItemRangeChanged(pos, groupList.size)
        }
    }
    inner class GroupViewHolder(val binding : ItemGroupRequestBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(group : GroupData) {
                    binding.tvGroupName.text = group.groupName
                    binding.tvGroupPeople.text = "${group.totalMembers}명"
                    binding.tvGroupInfo.text = group.description
                    binding.tvGroupTotal.text = "${group.totalMembers}시간"
                    binding.tvGroupStreak.text = "${group.streak}일"
                    var iconList = listOf(
                        R.drawable.ic_group_a,
                        R.drawable.ic_group_b,
                        R.drawable.ic_group_c,
                        R.drawable.ic_group_d,
                        R.drawable.ic_group_e,
                        R.drawable.ic_group_f,
                        R.drawable.ic_group_g,
                        R.drawable.ic_group_h,
                        R.drawable.ic_group_i,
                        R.drawable.ic_group_j,
                        R.drawable.ic_group_k,
                        R.drawable.ic_group_l
                    )
                    binding.imgGroupIcon.setImageResource(iconList[group.icon.toInt()])
                    binding.btnGroupAccept.setOnClickListener {
                        acceptEvent(group)
                    }
                    binding.btnGroupReject.setOnClickListener {
                        rejectEvent(group)
                    }
                }
            }
}
