package com.onlinestatusmonitor.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlinestatusmonitor.data.local.entity.ActivityLogEntity
import com.onlinestatusmonitor.databinding.ItemLogBinding
import com.onlinestatusmonitor.util.Formatters

class LogsAdapter : ListAdapter<ActivityLogEntity, LogsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ActivityLogEntity>() {
        override fun areItemsTheSame(oldItem: ActivityLogEntity, newItem: ActivityLogEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ActivityLogEntity, newItem: ActivityLogEntity) =
            oldItem == newItem
    }

    inner class VH(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ActivityLogEntity) {
            binding.tvLogMessage.text = item.message
            binding.tvLogTime.text = Formatters.formatDateTime(item.timestampEpochMs)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

