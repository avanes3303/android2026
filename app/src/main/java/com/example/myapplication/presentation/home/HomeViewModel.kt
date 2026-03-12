package com.example.myapplication.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.repository.HabitRepository

class HomeViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    private val _challenges = MutableLiveData<List<HabitDisplayItem>>()
    val challenges: LiveData<List<HabitDisplayItem>> = _challenges

    init {
        loadChallenges()
    }

    private fun loadChallenges() {
        _challenges.value = repository.getChallenges()
    }

    fun addChallenge(title: String, emoji: String, category: String) {
        val item = HabitDisplayItem(
            emoji = emoji,
            title = title,
            description = "Мой челлендж",
            category = category,
            streakDays = 0,
            completionPercent = 0,
            targetDays = 30
        )
        repository.addChallenge(item)
        _challenges.value = repository.getChallenges()
    }

    fun completeDay(title: String) {
        repository.completeDay(title)
        _challenges.value = repository.getChallenges()
    }
}
