package com.onlinestatusmonitor.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlinestatusmonitor.R
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity
import com.onlinestatusmonitor.databinding.ItemNumberBinding

class NumbersAdapter(
    private val onClick: (MonitoredNumberEntity) -> Unit
) : ListAdapter<MonitoredNumberEntity, NumbersAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<MonitoredNumberEntity>() {
        override fun areItemsTheSame(oldItem: MonitoredNumberEntity, newItem: MonitoredNumberEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MonitoredNumberEntity, newItem: MonitoredNumberEntity) =
            oldItem == newItem
    }

    inner class VH(private val binding: ItemNumberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MonitoredNumberEntity) {
            binding.tvNumber.text = item.display()
            binding.tvStatus.text = if (item.isOnline) "ONLINE" else "OFFLINE"
            binding.statusDot.setBackgroundResource(
                if (item.isOnline) R.drawable.status_dot_online else R.drawable.status_dot_offline
            )
            binding.root.setOnClickListener { onClick(item) }

            // Small cyber-like emphasis animation when binding (smooth on S22 Ultra).
            binding.root.alpha = 0f
            binding.root.animate().alpha(1f).setDuration(220).start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

