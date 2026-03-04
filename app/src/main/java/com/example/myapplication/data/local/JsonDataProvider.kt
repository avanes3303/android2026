package com.example.myapplication.data.local

import android.content.Context
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.model.ProfileStat
import com.example.myapplication.domain.model.UserProfile
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

// =============================================================================
// ЛАБА 5 + задел на Л7 (SDUI): Чтение данных из JSON-файлов в assets/
//
// Данные вынесены из кода в JSON — легко менять без перекомпиляции.
// В будущем (Л7) JSON будет приходить с сервера.
//
// Используется Application Context (не Activity) — безопасно хранить
// в синглтоне, не вызывает утечек памяти.
// =============================================================================

class JsonDataProvider(private val context: Context) {

    private val gson = Gson()

    fun loadChallenges(): List<HabitDisplayItem> {
        val json = readAsset("challenges.json")
        val type = object : TypeToken<List<ChallengeJson>>() {}.type
        val items: List<ChallengeJson> = gson.fromJson(json, type)
        return items.map { it.toDomain() }
    }

    fun loadProfile(): UserProfile {
        val json = readAsset("profile.json")
        val profileJson: ProfileJson = gson.fromJson(json, ProfileJson::class.java)
        return profileJson.toDomain()
    }

    private fun readAsset(fileName: String): String =
        context.assets.open(fileName).bufferedReader().use { it.readText() }
}

// --- Внутренние DTO для JSON-парсинга (не утекают наружу) ---

private data class ChallengeJson(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val category: String,
    @SerializedName("target_days")
    val targetDays: Int,
    @SerializedName("streak_days")
    val streakDays: Int,
    @SerializedName("completion_percent")
    val completionPercent: Int
) {
    fun toDomain() = HabitDisplayItem(
        emoji = emoji,
        title = title,
        description = description,
        category = category,
        streakDays = streakDays,
        completionPercent = completionPercent,
        targetDays = targetDays
    )
}

private data class ProfileJson(
    val name: String,
    @SerializedName("member_since")
    val memberSince: String,
    val stats: List<ProfileStatJson>
) {
    fun toDomain() = UserProfile(
        name = name,
        memberSince = memberSince,
        stats = stats.map { ProfileStat(it.label, it.value) }
    )
}

private data class ProfileStatJson(
    val label: String,
    val value: String
)
