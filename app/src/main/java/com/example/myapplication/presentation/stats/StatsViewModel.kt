package com.example.myapplication.presentation.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.ApiResult
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.repository.HabitRepository
import com.example.myapplication.domain.repository.QuoteRepository
import kotlinx.coroutines.launch

class StatsViewModel(
    private val habitRepository: HabitRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _quoteState = MutableLiveData<ApiResult<String>>(ApiResult.Loading)
    val quoteState: LiveData<ApiResult<String>> = _quoteState

    private val _habits = MutableLiveData<List<HabitDisplayItem>>()
    val habits: LiveData<List<HabitDisplayItem>> = _habits

    private val _summaryCount = MutableLiveData<String>()
    val summaryCount: LiveData<String> = _summaryCount

    private val _summaryStreak = MutableLiveData<String>()
    val summaryStreak: LiveData<String> = _summaryStreak

    private val _summaryAvg = MutableLiveData<String>()
    val summaryAvg: LiveData<String> = _summaryAvg

    init {
        loadHabits()
        loadQuote()
    }

    fun loadQuote() {
        _quoteState.value = ApiResult.Loading

        viewModelScope.launch {
            _quoteState.value = quoteRepository.getMotivationalQuote()
        }
    }

    private fun loadHabits() {
        val items = habitRepository.getChallenges()
        _habits.value = items

        // Вычисляем сводку динамически из данных
        _summaryCount.value = items.size.toString()
        _summaryStreak.value = "${items.maxOfOrNull { it.streakDays } ?: 0} дн"
        _summaryAvg.value = "${items.map { it.completionPercent }.average().toInt()}%"
    }
}
