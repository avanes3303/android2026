package com.example.myapplication.presentation.stats.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemHabitBinding
import com.example.myapplication.domain.model.HabitDisplayItem

// =============================================================================
// ЛАБА 4: RecyclerView Adapter
//
// Демонстрация:
//  - ListAdapter с DiffUtil.ItemCallback для эффективного обновления
//  - ViewHolder с View Binding (не itemView.findViewById)
//  - inflate с attachToRoot = false (обязательное требование)
//  - Отступы и размеры из dimens.xml
// =============================================================================

class HabitAdapter(
    private val onItemClick: (HabitDisplayItem) -> Unit = {}
) : ListAdapter<HabitDisplayItem, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        // ЛАБА 4: inflate с parent и attachToRoot = false
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false   // attachToRoot = false — обязательно для RecyclerView
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder с View Binding
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
            binding.tvHabitPercent.text = binding.root.context.getString(
                R.string.stats_percent_format, item.completionPercent
            )
            binding.tvHabitPercent.setTextColor(
                ContextCompat.getColor(binding.root.context, completionColorRes(item.completionPercent))
            )
            binding.progressHabit.progress = item.completionPercent
        }

        private fun completionColorRes(percent: Int): Int = when {
            percent >= 80 -> R.color.completion_high
            percent >= 50 -> R.color.completion_medium
            else -> R.color.completion_low
        }
    }
}

// ЛАБА 4: DiffUtil для эффективного обновления списка
class HabitDiffCallback : DiffUtil.ItemCallback<HabitDisplayItem>() {
    override fun areItemsTheSame(old: HabitDisplayItem, new: HabitDisplayItem): Boolean =
        old.title == new.title

    override fun areContentsTheSame(old: HabitDisplayItem, new: HabitDisplayItem): Boolean =
        old == new
}
