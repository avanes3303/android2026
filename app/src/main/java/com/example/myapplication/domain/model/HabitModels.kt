package com.example.myapplication.domain.model

// =============================================================================
// ЛАБА 1: Kotlin — бизнес-модели трекера привычек
//
// Демонстрация ключевых концепций языка:
//  §1  — Классы с primary / secondary конструктором
//  §2  — Data-классы (copy, деструктуризация)
//  §3  — Наследование (open, abstract, override, final override, super)
//  §6  — Sealed-классы (состояния + when без else)
//  §7  — Enum-классы (с конструктором, с анонимным классом)
//  §8  — Вложенные (nested) и внутренние (inner) классы
//  §12 — Nullable-типы (?., ?:, let, !!)
// =============================================================================

// ---------------------------------------------------------------------------
// §7: ENUM CLASS — с конструктором и анонимным классом
// ---------------------------------------------------------------------------

enum class HabitFrequency(val displayName: String, val timesPerWeek: Int) {
    DAILY("Каждый день", 7),
    WEEKDAYS("По будням", 5),
    WEEKLY("Раз в неделю", 1);

    fun isMoreFrequentThan(other: HabitFrequency): Boolean =
        this.timesPerWeek > other.timesPerWeek
}

enum class ChallengeCategory(val emoji: String, val displayName: String) {
    FITNESS("💪", "Спорт") {
        // §7: Анонимный класс — одна константа переопределяет метод
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

// ---------------------------------------------------------------------------
// §6: SEALED CLASS — состояния челленджа
// when без else — компилятор проверяет полноту вариантов
// ---------------------------------------------------------------------------

sealed class ChallengeState {
    // object — синглтон-состояние (без данных)
    object NotStarted : ChallengeState()

    // data class внутри sealed — содержит данные о прогрессе
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

// Sealed class для результата API-запроса (используется как UI State в Л3)
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// ---------------------------------------------------------------------------
// §3: ABSTRACT CLASS — наследование
// Базовый класс для всех сущностей. Демонстрирует open/abstract/override/super.
// ---------------------------------------------------------------------------

abstract class BaseEntity(
    open val id: String,
    open val createdAt: Long = System.currentTimeMillis()
) {
    abstract fun toDisplayString(): String

    // open — может быть переопределён наследниками
    open fun entityType(): String = "BaseEntity"

    override fun toString(): String = toDisplayString()
}

// ---------------------------------------------------------------------------
// §1 + §2 + §3 + §4 + §12: DATA CLASS (User)
// Primary constructor, наследование, реализация двух интерфейсов,
// final override, nullable-поля
// ---------------------------------------------------------------------------

data class User(
    override val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,          // §12: nullable-поле
    val memberSince: String? = null,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseEntity(id, createdAt), Displayable, Trackable {

    override val trackingId: String get() = id

    // §3: final override — запрещает дальнейшее переопределение в наследниках
    final override fun toDisplayString(): String = "User(name=$name, email=$email)"

    override fun displayInfo(): String {
        // §12: ?. (safe call) + ?: (Elvis) + let
        val avatar = avatarUrl?.let { url ->
            "Аватар: $url"
        } ?: "Аватар не задан"
        return "$name ($email) — $avatar"
    }

    // Computed property
    val displayName: String get() = name.ifBlank { email.substringBefore("@") }

    // §3: override + super — вызов метода родительского класса
    override fun entityType(): String = "${super.entityType()}/User"
}

// ---------------------------------------------------------------------------
// §1: CLASS С SECONDARY CONSTRUCTOR
// Дополнительные конструкторы для удобного создания — делегирование через this(...)
// ---------------------------------------------------------------------------

data class HabitEntry(
    override val id: String,
    val challengeId: String,
    val timestamp: Long,
    val isCompleted: Boolean,
    val note: String = ""
) : BaseEntity(id, timestamp) {

    // Secondary constructor — делегирует в primary через this(...)
    // Логически оправдан: позволяет создать запись без ручного указания id и timestamp
    constructor(challengeId: String, completed: Boolean) : this(
        id = "${challengeId}_${System.currentTimeMillis()}",
        challengeId = challengeId,
        timestamp = System.currentTimeMillis(),
        isCompleted = completed
    )

    // Ещё один secondary constructor — добавляет заметку
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

// ---------------------------------------------------------------------------
// §8 + §4: DATA CLASS с NESTED CLASS + реализация интерфейсов
// Challenge реализует Displayable + Trackable (два интерфейса одновременно)
// Содержит nested class Metadata и inner class ProgressTracker
// ---------------------------------------------------------------------------

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

    // §4: Реализация интерфейсов
    override val trackingId: String get() = id
    override val summaryTitle: String get() = "${category.emoji} $title"

    override fun isActive(): Boolean = state.isActive()

    override fun displayInfo(): String =
        "$summaryTitle — ${state.javaClass.simpleName}"

    // §8: NESTED CLASS (без inner) — не держит ссылку на внешний класс
    // Логически: Metadata — это просто контейнер данных, не зависит от Challenge
    data class Metadata(
        val color: String = "#6200EE",
        val isPublic: Boolean = true,
        val tags: List<String> = emptyList(),
        val iconEmoji: String = "⭐"
    )

    // §8: INNER CLASS — держит ссылку на внешний класс (обращается к полям Challenge)
    // Логически: ProgressTracker работает именно с этим челленджем
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

// ---------------------------------------------------------------------------
// DATA CLASS для статистики
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// §14: OBJECT как фабрика / валидатор (синглтон)
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// DTO для отображения привычки в списке (используется в UI)
// ---------------------------------------------------------------------------

data class HabitDisplayItem(
    val emoji: String,
    val title: String,
    val description: String,
    val category: String,
    val streakDays: Int,
    val completionPercent: Int,
    val targetDays: Int
)

// ---------------------------------------------------------------------------
// DTO для отображения профиля
// ---------------------------------------------------------------------------

data class UserProfile(
    val name: String,
    val memberSince: String,
    val stats: List<ProfileStat>
)

data class ProfileStat(
    val label: String,
    val value: String
)
