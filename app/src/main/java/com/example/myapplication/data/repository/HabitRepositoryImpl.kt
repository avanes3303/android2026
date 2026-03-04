package com.example.myapplication.data.repository

import com.example.myapplication.data.local.JsonDataProvider
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.HabitRepository

// =============================================================================
// ЛАБА 5: Repository — реализация в data-слое
// Читает данные из JSON (через JsonDataProvider).
// В будущем легко заменить на Room, API или другой источник — UI не изменится.
// =============================================================================

class HabitRepositoryImpl(
    private val jsonDataProvider: JsonDataProvider
) : HabitRepository {

    // Кэш — чтобы не читать JSON при каждом обращении
    private val challengesCache by lazy { jsonDataProvider.loadChallenges() }
    private val profileCache by lazy { jsonDataProvider.loadProfile() }

    override fun getChallenges(): List<HabitDisplayItem> = challengesCache

    override fun getChallengeById(id: String): HabitDisplayItem? =
        challengesCache.firstOrNull { it.title == id }

    override fun getUserProfile(): UserProfile = profileCache
}
