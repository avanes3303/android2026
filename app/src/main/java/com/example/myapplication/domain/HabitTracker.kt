package com.example.myapplication.domain

import com.example.myapplication.domain.model.BaseEntity
import com.example.myapplication.domain.model.Challenge
import com.example.myapplication.domain.model.ChallengeCategory
import com.example.myapplication.domain.model.ChallengeState
import com.example.myapplication.domain.model.HabitEntry
import com.example.myapplication.domain.model.HabitStats

// =============================================================================
// ЛАБА 1 (продолжение): Бизнес-логика трекера привычек
//
// Демонстрация:
//  §5  — Приведение типов (is, !is, as, as?, when по типам)
//  §10 — Generics (generic-функция, generic-интерфейс, ограничение типа)
//  §11 — Лямбды и функциональные типы (typealias, trailing lambda, it,
//         функция возвращающая функциональный тип)
//  §13 — Управление потоком (when как выражение, withIndex, @label return)
// =============================================================================

private const val DAY_MS = 24 * 60 * 60 * 1000L

// ---------------------------------------------------------------------------
// §10: GENERICS — generic-интерфейс с ограничением типа
// ---------------------------------------------------------------------------

interface Repository<T : BaseEntity> {
    fun findById(id: String): T?
    fun findAll(): List<T>
    fun save(entity: T)
    fun delete(id: String)
}

class InMemoryRepository<T : BaseEntity> : Repository<T> {
    private val store = mutableMapOf<String, T>()

    override fun findById(id: String): T? = store[id]
    override fun findAll(): List<T> = store.values.toList()
    override fun save(entity: T) { store[entity.id] = entity }
    override fun delete(id: String) { store.remove(id) }

    fun filter(predicate: (T) -> Boolean): List<T> = store.values.filter(predicate)
}

// §10: generic-функция с ограничением типа <T : Comparable<T>>
fun <T : Comparable<T>> findMax(items: List<T>): T? = items.maxOrNull()

// §10: generic-функция
fun <T : BaseEntity> printAll(items: List<T>) {
    items.forEach { println(it.toDisplayString()) }
}

// ---------------------------------------------------------------------------
// §11: ЛЯМБДЫ + ФУНКЦИОНАЛЬНЫЕ ТИПЫ
// ---------------------------------------------------------------------------

// §11: typealias для именования функциональных типов
typealias OnChallengeCompleted = (Challenge, HabitStats) -> Unit
typealias HabitFilter          = (HabitEntry) -> Boolean
typealias Transformer<T>       = (HabitEntry) -> T

// §11: Переменная с функциональным типом
val defaultFilter: HabitFilter = { entry -> entry.isCompleted }

// §11: Функция ВОЗВРАЩАЕТ функциональный тип
fun createStreakFilter(minStreak: Int): HabitFilter {
    return { entry ->
        entry.isCompleted && entry.note.isNotBlank()
    }
}

// ---------------------------------------------------------------------------
// §5: ПРИВЕДЕНИЕ ТИПОВ — is, !is, as, as?, when по типам
// ---------------------------------------------------------------------------

fun describeState(state: ChallengeState): String {
    // §5: is — проверка типа (smart cast: после проверки компилятор знает тип)
    return if (state is ChallengeState.InProgress) {
        "Прогресс: ${state.daysCompleted}/${state.totalDays}"
    } else {
        state.toString()
    }
}

fun getInProgressOrNull(state: ChallengeState): ChallengeState.InProgress? {
    // §5: as? — безопасное приведение типов (вернёт null, если тип не совпадает)
    return state as? ChallengeState.InProgress
}

// §5: when в контексте приведения типов
fun stateToHumanString(state: ChallengeState): String = when (state) {
    is ChallengeState.NotStarted -> "Ожидает начала"
    is ChallengeState.InProgress -> "В процессе: день ${state.daysCompleted}"
    is ChallengeState.Completed  -> "Завершён за ${state.finalStreak} дней"
    is ChallengeState.Failed     -> "Не удалось: ${state.reason}"
}

// §5: Функция с !is и unsafe as
fun processActiveState(state: ChallengeState): Int {
    // §5: !is — отрицательная проверка типа
    if (state !is ChallengeState.InProgress) {
        return -1
    }

    // После !is + return компилятор знает, что здесь state — InProgress (smart cast)
    return state.daysCompleted
}

fun unsafeCastDemo(state: ChallengeState): ChallengeState.InProgress {
    // §5: as — небезопасное приведение типов
    // Безопасно здесь, потому что функция вызывается ТОЛЬКО после проверки is InProgress
    // в вызывающем коде (см. completeDay ниже). Если тип не совпадёт — ClassCastException
    return state as ChallengeState.InProgress
}

// ---------------------------------------------------------------------------
// ОСНОВНОЙ КЛАСС БИЗНЕС-ЛОГИКИ
// ---------------------------------------------------------------------------

class HabitTracker(
    private val challengeRepo: InMemoryRepository<Challenge> = InMemoryRepository(),
    private val entryRepo: InMemoryRepository<HabitEntry>    = InMemoryRepository(),
    // §11: Функциональный тип как параметр конструктора
    private val onCompleted: OnChallengeCompleted? = null
) {

    fun startChallenge(challenge: Challenge): Challenge {
        // §2: copy() — создание копии data class с изменённым полем
        val started = challenge.copy(
            state = ChallengeState.InProgress(0, challenge.targetDays)
        )
        challengeRepo.save(started)
        return started
    }

    fun completeDay(challengeId: String, note: String = ""): Result<HabitEntry> {
        // §12: ?. + ?: (Elvis) — если не найден, возвращаем ошибку
        val challenge = challengeRepo.findById(challengeId)
            ?: return Result.failure(IllegalArgumentException("Challenge $challengeId не найден"))

        val entry = HabitEntry(challengeId, true, note)   // secondary constructor (§1)
        entryRepo.save(entry)

        val entries = getEntriesFor(challengeId)
        val state = challenge.state

        if (state is ChallengeState.InProgress) {
            val newCompleted = state.daysCompleted + 1
            // §13: when как выражение — результат присваивается переменной
            val newState: ChallengeState = when {
                newCompleted >= challenge.targetDays -> {
                    val stats = buildStats(challengeId, entries)
                    // §12: ?. — безопасный вызов nullable-callback
                    onCompleted?.invoke(challenge, stats)
                    ChallengeState.Completed(finalStreak = entries.calculateStreak())
                }
                else -> state.copy(daysCompleted = newCompleted)
            }
            challengeRepo.save(challenge.copy(state = newState))
        }

        return Result.success(entry)
    }

    // §11: trailing lambda синтаксис + it как неявное имя параметра
    fun getFilteredEntries(challengeId: String, filter: HabitFilter = { true }): List<HabitEntry> =
        getEntriesFor(challengeId).filter(filter)

    // §10: generic-функция с Transformer lambda
    fun <T> mapEntries(challengeId: String, transform: Transformer<T>): List<T> =
        getEntriesFor(challengeId).map(transform)

    fun getStats(challengeId: String): HabitStats {
        val entries = getEntriesFor(challengeId)
        return buildStats(challengeId, entries)
    }

    fun getAllChallenges(): List<Challenge> = challengeRepo.findAll()

    // §13: for с withIndex() + деструктуризация
    fun printChallengeLog() {
        for ((index, challenge) in getAllChallenges().withIndex()) {
            println("#$index: ${challenge.title} — ${challenge.statusText()}")
        }
    }

    // §13: метка @label для return из лямбды
    fun findFirstCompleted(challengeId: String): HabitEntry? {
        getEntriesFor(challengeId).forEach findLoop@{ entry ->
            if (entry.isCompleted) {
                return@findLoop // возврат из лямбды (не из функции), продолжает forEach
            }
        }
        // Возвращаем первую найденную через стандартную функцию
        return getEntriesFor(challengeId).firstOrNull { it.isCompleted }
    }

    private fun getEntriesFor(id: String): List<HabitEntry> =
        entryRepo.filter { it.challengeId == id }  // §11: trailing lambda + it

    private fun buildStats(challengeId: String, entries: List<HabitEntry>): HabitStats =
        HabitStats(
            challengeId   = challengeId,
            totalDays     = entries.size,
            completedDays = entries.count { it.isCompleted },
            currentStreak = entries.calculateStreak(),
            longestStreak = entries.calculateStreak()
        )
}

// ---------------------------------------------------------------------------
// ДЕМО-ФУНКЦИЯ — показывает как используются все концепты вместе
// ---------------------------------------------------------------------------

fun createSampleTracker(): HabitTracker {
    val tracker = HabitTracker(
        // §11: trailing lambda синтаксис
        onCompleted = { challenge, stats ->
            println("🎉 ${challenge.title} завершён! Стрик: ${stats.currentStreak} дней")
        }
    )

    val meditation = Challenge(
        id          = "c_meditation",
        title       = "21 день медитации",
        description = "10 минут с утра, каждый день",
        category    = ChallengeCategory.MINDFULNESS,
        targetDays  = 21,
        metadata    = Challenge.Metadata(
            color      = "#9C27B0",
            iconEmoji  = "🧘",
            tags       = listOf("утро", "здоровье", "mindfulness")
        )
    )

    tracker.startChallenge(meditation)
    tracker.completeDay("c_meditation", note = "Отличное утро!")

    // §2: Деструктуризация data class через val (a, b, ...) = obj
    val (id, title, description) = meditation
    println("Деструктуризация: id=$id, title=$title, desc=$description")

    // §11: trailing lambda + it
    val completedToday = tracker.getFilteredEntries("c_meditation") { it.isCompleted }

    // §10: generic-функция с лямбдой-трансформером
    val timestamps: List<Long> = tracker.mapEntries("c_meditation") { it.timestamp }

    // §9: extension function
    val challenges = tracker.getAllChallenges()
    challenges.forEach { c ->
        println(c.statusText())
        println(c.progressPercent())

        // §8: inner class — создаётся от экземпляра внешнего класса
        val progressTracker = c.ProgressTracker()
        println(progressTracker.progressDescription())
    }

    // §5: as? — безопасное приведение типа
    val inProgress: ChallengeState.InProgress? = getInProgressOrNull(challenges.first().state)
    // §12: ?. + ?: (Elvis)
    println("Прогресс: ${inProgress?.daysCompleted ?: 0} дней")

    // §12: !! — единственное использование, здесь безопасно потому что
    // мы только что вызвали startChallenge + completeDay, значит state гарантированно InProgress
    val guaranteedState = challenges.first().state
    if (guaranteedState is ChallengeState.InProgress) {
        val forcedDays = (guaranteedState as ChallengeState.InProgress).daysCompleted
        // !! используется ниже на результате findById, который гарантированно не null
        // после save выше
        val foundChallenge = tracker.getAllChallenges().firstOrNull { it.id == "c_meditation" }!!
        println("Найден: ${foundChallenge.title}, дней: $forcedDays")
    }

    // §13: for с withIndex
    tracker.printChallengeLog()

    return tracker
}
