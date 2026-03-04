package com.example.myapplication.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.repository.HabitRepository

// =============================================================================
// ЛАБА 5: ViewModel для HomeFragment
//
// ViewModel переживает поворот экрана — данные не теряются.
// НЕ держит ссылку на Context, View или Fragment.
// LiveData для передачи данных во Fragment (observe pattern).
// =============================================================================

class HomeViewModel(
    private val repository: HabitRepository  // DI через конструктор
) : ViewModel() {

    private val _challenges = MutableLiveData<List<HabitDisplayItem>>()
    val challenges: LiveData<List<HabitDisplayItem>> = _challenges

    init {
        loadChallenges()
    }

    private fun loadChallenges() {
        _challenges.value = repository.getChallenges()
    }
}
