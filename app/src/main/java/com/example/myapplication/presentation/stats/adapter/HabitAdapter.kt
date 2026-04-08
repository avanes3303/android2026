package com.example.myapplication.presentation.stats.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemHabitBinding
import com.example.myapplication.domain.model.HabitDisplayItem

class HabitAdapter(
    private val onItemClick: (HabitDisplayItem) -> Unit = {}
) : ListAdapter<HabitDisplayItem, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: HabitDisplayItem) {
            binding.tvHabitTitle.text = "${item.emoji} ${item.title}"
            binding.tvHabitStreak.text = binding.root.context.getString(
                R.string.stats_streak_days, item.streakDays
            )
            binding.circularProgress.setProgress(item.completionPercent)
            binding.progressHabit.progress = item.completionPercent
        }

    }
}

class HabitDiffCallback : DiffUtil.ItemCallback<HabitDisplayItem>() {
    override fun areItemsTheSame(old: HabitDisplayItem, new: HabitDisplayItem): Boolean =
        old.title == new.title

    override fun areContentsTheSame(old: HabitDisplayItem, new: HabitDisplayItem): Boolean =
        old == new
}
