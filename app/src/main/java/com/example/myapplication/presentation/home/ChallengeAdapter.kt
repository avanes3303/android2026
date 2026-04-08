package com.example.myapplication.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemChallengeBinding
import com.example.myapplication.domain.model.HabitDisplayItem

class ChallengeAdapter(
    private val onItemClick: (HabitDisplayItem) -> Unit = {}
) : ListAdapter<HabitDisplayItem, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChallengeViewHolder(
        private val binding: ItemChallengeBinding
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
            binding.tvChallengeTitle.text = "${item.emoji} ${item.title}"
            binding.tvChallengeDescription.text = item.description
            binding.circularProgress.setProgress(item.completionPercent)
        }
    }
}

class ChallengeDiffCallback : DiffUtil.ItemCallback<HabitDisplayItem>() {
    override fun areItemsTheSame(old: HabitDisplayItem, new: HabitDisplayItem): Boolean =
        old.title == new.title

    override fun areContentsTheSame(old: HabitDisplayItem, new: HabitDisplayItem): Boolean =
        old == new
}
