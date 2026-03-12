package com.example.myapplication.data.repository

import com.example.myapplication.data.local.JsonDataProvider
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.HabitRepository

class HabitRepositoryImpl(
    private val jsonDataProvider: JsonDataProvider
) : HabitRepository {

    // Кэш — чтобы не читать JSON при каждом обращении
    private val challengesCache: MutableList<HabitDisplayItem>
    private val profileCache by lazy { jsonDataProvider.loadProfile() }

    init {
        challengesCache = jsonDataProvider.loadChallenges().toMutableList()
    }

    override fun getChallenges(): List<HabitDisplayItem> = challengesCache.toList()

    override fun getChallengeById(id: String): HabitDisplayItem? =
        challengesCache.firstOrNull { it.title == id }

    override fun getUserProfile(): UserProfile = profileCache

    override fun addChallenge(item: HabitDisplayItem) {
        challengesCache.add(item)
    }

    override fun completeDay(title: String) {
        val index = challengesCache.indexOfFirst { it.title == title }
        if (index != -1) {
            val item = challengesCache[index]
            val newStreak = item.streakDays + 1
            val newPercent = if (item.targetDays > 0) {
                (newStreak * 100) / item.targetDays
            } else {
                0
            }
            challengesCache[index] = item.copy(
                streakDays = newStreak,
                completionPercent = newPercent.coerceAtMost(100)
            )
        }
    }
}
