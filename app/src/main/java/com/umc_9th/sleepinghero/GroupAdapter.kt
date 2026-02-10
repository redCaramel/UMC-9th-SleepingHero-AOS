package com.umc_9th.sleepinghero

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc_9th.sleepinghero.databinding.ItemGroupBinding

class GroupAdapter(private var groupList : MutableList<GroupData>,
                  private val detailEvent: (GroupData) -> Unit,
                   private val inviteEvent: (GroupData) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(
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

    fun updateList(newList : List<GroupData>) {
        groupList.clear()
        groupList.addAll(newList)
        Log.d("test", "${newList.size}")
        notifyDataSetChanged()
    }
    inner class GroupViewHolder(val binding : ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group : GroupData) {
            binding.tvGroupName.text = group.groupName
            binding.tvGroupInfo.text = group.description
            binding.tvGroupTotal.text = "${group.totalTime}시간"
            binding.tvGroupStreak.text = "${group.streak}일"
            binding.tvGroupPeople.text = "${group.totalMembers}명"
            binding.tvGroupLeader.text = group.leader
            binding.btnGroupDetail.setOnClickListener {
                detailEvent(group)
            }
            binding.btnGroupInvite.setOnClickListener {
                inviteEvent(group)
            }
        }
    }
}
