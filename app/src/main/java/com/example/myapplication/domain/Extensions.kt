package com.example.myapplication.domain

import com.example.myapplication.domain.model.Challenge
import com.example.myapplication.domain.model.ChallengeState
import com.example.myapplication.domain.model.HabitEntry

private const val DAY_MS = 24 * 60 * 60 * 1000L

fun Challenge.statusText(): String = when (val s = state) {
    is ChallengeState.NotStarted -> "Не начат"
    is ChallengeState.InProgress -> "День ${s.daysCompleted} из ${s.totalDays}"
    is ChallengeState.Completed  -> "Завершён! Стрик: ${s.finalStreak} дней"
    is ChallengeState.Failed     -> "Прерван: ${s.reason}"
}

fun Challenge.progressPercent(): Int = when (val s = state) {
    is ChallengeState.InProgress -> (s.daysCompleted * 100) / s.totalDays.coerceAtLeast(1)
    is ChallengeState.Completed  -> 100
    else                         -> 0
}

fun List<HabitEntry>.calculateStreak(): Int {
    val today = System.currentTimeMillis() / DAY_MS
    val completedDays = this
        .filter { it.isCompleted }
        .map { it.timestamp / DAY_MS }
        .toSortedSet()

    if (completedDays.isEmpty()) return 0
    if (today - completedDays.max() > 1) return 0

    var streak = 0
    var day = today
    while (completedDays.contains(day)) {
        streak++
        day--
    }
    return streak
}

fun List<HabitEntry>.completionRate(): Float =
    if (isEmpty()) 0f else count { it.isCompleted }.toFloat() / size

val List<HabitEntry>.averageCompletionPercent: Int
    get() = if (isEmpty()) 0 else (completionRate() * 100).toInt()

fun String.toSlug(): String = this
    .lowercase()
    .replace(Regex("[^a-zа-я0-9\\s-]"), "")
    .replace(Regex("\\s+"), "-")
    .trim('-')

fun Int.toDaysString(): String = when {
    this % 10 == 1 && this % 100 != 11 -> "$this день"
    this % 10 in 2..4 && this % 100 !in 12..14 -> "$this дня"
    else -> "$this дней"
}
