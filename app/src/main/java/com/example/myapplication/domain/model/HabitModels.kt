package com.example.myapplication.domain.model

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

data class HabitDisplayItem(
    val emoji: String,
    val title: String,
    val description: String,
    val category: String,
    val streakDays: Int,
    val completionPercent: Int,
    val targetDays: Int
)

data class UserProfile(
    val name: String,
    val memberSince: String,
    val stats: List<ProfileStat>
)

data class ProfileStat(
    val label: String,
    val value: String
)
