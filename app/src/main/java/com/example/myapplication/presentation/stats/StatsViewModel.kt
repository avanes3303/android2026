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

// =============================================================================
// ЛАБА 3 + ЛАБА 5: ViewModel + LiveData + Coroutines
//
// ViewModel — хранит UI-состояние, переживает повороты экрана.
// LiveData — реактивные данные: UI (Fragment) подписывается через observe().
// viewModelScope — корутина привязана к жизненному циклу ViewModel.
// ViewModel НЕ держит ссылку на Context, View или Fragment.
// =============================================================================

class StatsViewModel(
    private val habitRepository: HabitRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    // --- Состояние цитаты (из API) ---
    private val _quoteState = MutableLiveData<ApiResult<String>>(ApiResult.Loading)
    val quoteState: LiveData<ApiResult<String>> = _quoteState

    // --- Список привычек (из JSON) ---
    private val _habits = MutableLiveData<List<HabitDisplayItem>>()
    val habits: LiveData<List<HabitDisplayItem>> = _habits

    // --- Сводная статистика (вычисляется из данных, не хардкод) ---
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
