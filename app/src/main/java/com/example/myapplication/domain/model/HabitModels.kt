package com.example.myapplication.domain.model

enum class HabitFrequency(val displayName: String, val timesPerWeek: Int) {
    DAILY("Каждый день", 7),
    WEEKDAYS("По будням", 5),
    WEEKLY("Раз в неделю", 1);

    fun isMoreFrequentThan(other: HabitFrequency): Boolean =
        this.timesPerWeek > other.timesPerWeek
}

enum class ChallengeCategory(val emoji: String, val displayName: String) {
    FITNESS("💪", "Спорт") {
        override fun motivationalPhrase(): String = "Движение — это жизнь!"
    },
    MINDFULNESS("🧘", "Медитация"),
    LEARNING("📚", "Обучение"),
    NUTRITION("🥗", "Питание"),
    SLEEP("😴", "Сон"),
    CREATIVITY("🎨", "Творчество");

    // Метод по умолчанию — FITNESS переопределяет его выше
    open fun motivationalPhrase(): String = "Продолжай в том же духе!"
}

sealed class ChallengeState {
    object NotStarted : ChallengeState()

    data class InProgress(
        val daysCompleted: Int,
        val totalDays: Int
    ) : ChallengeState()

    data class Completed(
        val finalStreak: Int,
        val completedAt: Long = System.currentTimeMillis()
    ) : ChallengeState()

    data class Failed(
        val lastActiveDay: Long,
        val reason: String
    ) : ChallengeState()

    fun isActive(): Boolean = this is InProgress
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

abstract class BaseEntity(
    open val id: String,
    open val createdAt: Long = System.currentTimeMillis()
) {
    abstract fun toDisplayString(): String

    open fun entityType(): String = "BaseEntity"

    override fun toString(): String = toDisplayString()
}

data class User(
    override val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val memberSince: String? = null,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseEntity(id, createdAt), Displayable, Trackable {

    override val trackingId: String get() = id

    override fun toDisplayString(): String = "User(name=$name, email=$email)"

    override fun displayInfo(): String {
        val avatar = avatarUrl?.let { url ->
            "Аватар: $url"
        } ?: "Аватар не задан"
        return "$name ($email) — $avatar"
    }

    val displayName: String get() = name.ifBlank { email.substringBefore("@") }

    override fun entityType(): String = "${super.entityType()}/User"
}

data class HabitEntry(
    override val id: String,
    val challengeId: String,
    val timestamp: Long,
    val isCompleted: Boolean,
    val note: String = ""
) : BaseEntity(id, timestamp) {

    constructor(challengeId: String, completed: Boolean) : this(
        id = "${challengeId}_${System.currentTimeMillis()}",
        challengeId = challengeId,
        timestamp = System.currentTimeMillis(),
        isCompleted = completed
    )

    constructor(challengeId: String, completed: Boolean, note: String) : this(
        id = "${challengeId}_${System.currentTimeMillis()}",
        challengeId = challengeId,
        timestamp = System.currentTimeMillis(),
        isCompleted = completed,
        note = note
    )

    override fun toDisplayString(): String =
        "HabitEntry(challenge=$challengeId, completed=$isCompleted)"

    override fun entityType(): String = "HabitEntry"
}

data class Challenge(
    override val id: String,
    val title: String,
    val description: String,
    val category: ChallengeCategory,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDays: Int = 30,
    val state: ChallengeState = ChallengeState.NotStarted,
    val metadata: Metadata = Metadata()
) : BaseEntity(id), Displayable, Trackable, Summarizable {

    override val trackingId: String get() = id
    override val summaryTitle: String get() = "${category.emoji} $title"

    override fun isActive(): Boolean = state.isActive()

    override fun displayInfo(): String =
        "$summaryTitle — ${state.javaClass.simpleName}"

    // Nested class — не держит ссылку на внешний класс
    data class Metadata(
        val color: String = "#6200EE",
        val isPublic: Boolean = true,
        val tags: List<String> = emptyList(),
        val iconEmoji: String = "⭐"
    )

    // Inner class — держит ссылку на внешний класс (обращается к полям Challenge)
    inner class ProgressTracker {
        fun progressPercent(): Int = when (val s = state) {
            is ChallengeState.InProgress -> (s.daysCompleted * 100) / s.totalDays.coerceAtLeast(1)
            is ChallengeState.Completed -> 100
            else -> 0
        }

        // Обращение к полям внешнего класса через неявный this@Challenge
        fun progressDescription(): String =
            "Челлендж «${this@Challenge.title}»: ${progressPercent()}%"
    }

    override fun toDisplayString(): String =
        "Challenge(title=$title, category=${category.displayName}, state=$state)"

    override fun entityType(): String = "Challenge"
}

data class HabitStats(
    val challengeId: String,
    val totalDays: Int,
    val completedDays: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val completionRate: Float
        get() = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f

    val completionPercent: Int get() = (completionRate * 100).toInt()
}

object ChallengeValidator {
    fun validate(title: String, targetDays: Int): ValidationResult {
        val errors = mutableListOf<String>()
        if (title.isBlank()) errors.add("Название не может быть пустым")
        if (targetDays < 1) errors.add("Минимум 1 день")
        if (targetDays > 365) errors.add("Максимум 365 дней")
        return if (errors.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(errors)
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }
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
