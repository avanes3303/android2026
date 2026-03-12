package com.example.myapplication.presentation.stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentStatsBinding
import com.example.myapplication.domain.model.ApiResult
import com.example.myapplication.presentation.ViewModelFactory
import com.example.myapplication.presentation.detail.DetailActivity
import com.example.myapplication.presentation.stats.adapter.HabitAdapter

class StatsFragment : Fragment() {

    companion object {
        const val TAG = "StatsFragment"
    }

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        setupRecyclerView()
        setupRetryButton()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter { item ->
            startActivity(
                DetailActivity.newIntent(
                    requireContext(), item.emoji, item.title, item.description,
                    item.category, item.targetDays, item.streakDays, item.completionPercent
                )
            )
        }
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadQuote()
        }
    }

    private fun observeViewModel() {
        viewModel.quoteState.observe(viewLifecycleOwner) { state ->
            // Сбрасываем видимость
            binding.progressQuote.visibility = View.GONE
            binding.tvQuote.visibility = View.GONE
            binding.tvQuoteError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE

            when (state) {
                is ApiResult.Loading -> {
                    binding.progressQuote.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    binding.tvQuote.visibility = View.VISIBLE
                    binding.tvQuote.text = state.data
                }
                is ApiResult.Error -> {
                    binding.tvQuoteError.visibility = View.VISIBLE
                    binding.tvQuoteError.text = "Не удалось загрузить: ${state.message}"
                    binding.btnRetry.visibility = View.VISIBLE
                }
            }
        }

        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.submitList(habits)
        }

        // Сводка — вычисляется динамически из данных
        viewModel.summaryCount.observe(viewLifecycleOwner) { count ->
            binding.summaryChips.tvChipCountValue.text = count
        }
        viewModel.summaryStreak.observe(viewLifecycleOwner) { streak ->
            binding.summaryChips.tvChipStreakValue.text = streak
        }
        viewModel.summaryAvg.observe(viewLifecycleOwner) { avg ->
            binding.summaryChips.tvChipAvgValue.text = avg
        }
    }
}
