package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.model.UserProfile

// =============================================================================
// ЛАБА 5, §5.2: Repository — ИНТЕРФЕЙС в domain-слое
// Реализация лежит в data/repository/HabitRepositoryImpl.kt
// Domain-слой не знает откуда приходят данные (JSON, сеть, БД).
// =============================================================================

interface HabitRepository {
    fun getChallenges(): List<HabitDisplayItem>
    fun getChallengeById(id: String): HabitDisplayItem?
    fun getUserProfile(): UserProfile
}
