package com.example.myapplication.domain

import com.example.myapplication.domain.model.BaseEntity
import com.example.myapplication.domain.model.Challenge
import com.example.myapplication.domain.model.ChallengeCategory
import com.example.myapplication.domain.model.ChallengeState
import com.example.myapplication.domain.model.HabitEntry
import com.example.myapplication.domain.model.HabitStats

private const val DAY_MS = 24 * 60 * 60 * 1000L

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

fun <T : Comparable<T>> findMax(items: List<T>): T? = items.maxOrNull()

fun <T : BaseEntity> printAll(items: List<T>) {
    items.forEach { println(it.toDisplayString()) }
}

typealias OnChallengeCompleted = (Challenge, HabitStats) -> Unit
typealias HabitFilter          = (HabitEntry) -> Boolean
typealias Transformer<T>       = (HabitEntry) -> T

val defaultFilter: HabitFilter = { entry -> entry.isCompleted }

fun createStreakFilter(minStreak: Int): HabitFilter {
    return { entry ->
        entry.isCompleted && entry.note.isNotBlank()
    }
}

fun describeState(state: ChallengeState): String {
    return if (state is ChallengeState.InProgress) {
        "Прогресс: ${state.daysCompleted}/${state.totalDays}"
    } else {
        state.toString()
    }
}

fun getInProgressOrNull(state: ChallengeState): ChallengeState.InProgress? {
    return state as? ChallengeState.InProgress
}

fun stateToHumanString(state: ChallengeState): String = when (state) {
    is ChallengeState.NotStarted -> "Ожидает начала"
    is ChallengeState.InProgress -> "В процессе: день ${state.daysCompleted}"
    is ChallengeState.Completed  -> "Завершён за ${state.finalStreak} дней"
    is ChallengeState.Failed     -> "Не удалось: ${state.reason}"
}

fun processActiveState(state: ChallengeState): Int {
    if (state !is ChallengeState.InProgress) {
        return -1
    }
    // После !is + return компилятор знает, что здесь state — InProgress (smart cast)
    return state.daysCompleted
}

fun unsafeCastDemo(state: ChallengeState): ChallengeState.InProgress {
    // Безопасно здесь, потому что функция вызывается ТОЛЬКО после проверки is InProgress
    // в вызывающем коде (см. completeDay ниже). Если тип не совпадёт — ClassCastException
    return state as ChallengeState.InProgress
}

class HabitTracker(
    private val challengeRepo: InMemoryRepository<Challenge> = InMemoryRepository(),
    private val entryRepo: InMemoryRepository<HabitEntry>    = InMemoryRepository(),
    private val onCompleted: OnChallengeCompleted? = null
) {

    fun startChallenge(challenge: Challenge): Challenge {
        val started = challenge.copy(
            state = ChallengeState.InProgress(0, challenge.targetDays)
        )
        challengeRepo.save(started)
        return started
    }

    fun completeDay(challengeId: String, note: String = ""): Result<HabitEntry> {
        val challenge = challengeRepo.findById(challengeId)
            ?: return Result.failure(IllegalArgumentException("Challenge $challengeId не найден"))

        val entry = HabitEntry(challengeId, true, note)
        entryRepo.save(entry)

        val entries = getEntriesFor(challengeId)
        val state = challenge.state

        if (state is ChallengeState.InProgress) {
            val newCompleted = state.daysCompleted + 1
            val newState: ChallengeState = when {
                newCompleted >= challenge.targetDays -> {
                    val stats = buildStats(challengeId, entries)
                    onCompleted?.invoke(challenge, stats)
                    ChallengeState.Completed(finalStreak = entries.calculateStreak())
                }
                else -> state.copy(daysCompleted = newCompleted)
            }
            challengeRepo.save(challenge.copy(state = newState))
        }

        return Result.success(entry)
    }

    fun getFilteredEntries(challengeId: String, filter: HabitFilter = { true }): List<HabitEntry> =
        getEntriesFor(challengeId).filter(filter)

    fun <T> mapEntries(challengeId: String, transform: Transformer<T>): List<T> =
        getEntriesFor(challengeId).map(transform)

    fun getStats(challengeId: String): HabitStats {
        val entries = getEntriesFor(challengeId)
        return buildStats(challengeId, entries)
    }

    fun getAllChallenges(): List<Challenge> = challengeRepo.findAll()

    fun printChallengeLog() {
        for ((index, challenge) in getAllChallenges().withIndex()) {
            println("#$index: ${challenge.title} — ${challenge.statusText()}")
        }
    }

    fun findFirstCompleted(challengeId: String): HabitEntry? {
        getEntriesFor(challengeId).forEach findLoop@{ entry ->
            if (entry.isCompleted) {
                return@findLoop
            }
        }
        return getEntriesFor(challengeId).firstOrNull { it.isCompleted }
    }

    private fun getEntriesFor(id: String): List<HabitEntry> =
        entryRepo.filter { it.challengeId == id }

    private fun buildStats(challengeId: String, entries: List<HabitEntry>): HabitStats =
        HabitStats(
            challengeId   = challengeId,
            totalDays     = entries.size,
            completedDays = entries.count { it.isCompleted },
            currentStreak = entries.calculateStreak(),
            longestStreak = entries.calculateStreak()
        )
}

fun createSampleTracker(): HabitTracker {
    val tracker = HabitTracker(
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

    val (id, title, description) = meditation
    println("Деструктуризация: id=$id, title=$title, desc=$description")

    val completedToday = tracker.getFilteredEntries("c_meditation") { it.isCompleted }

    val timestamps: List<Long> = tracker.mapEntries("c_meditation") { it.timestamp }

    val challenges = tracker.getAllChallenges()
    challenges.forEach { c ->
        println(c.statusText())
        println(c.progressPercent())

        val progressTracker = c.ProgressTracker()
        println(progressTracker.progressDescription())
    }

    val inProgress: ChallengeState.InProgress? = getInProgressOrNull(challenges.first().state)
    println("Прогресс: ${inProgress?.daysCompleted ?: 0} дней")

    val guaranteedState = challenges.first().state
    if (guaranteedState is ChallengeState.InProgress) {
        val forcedDays = (guaranteedState as ChallengeState.InProgress).daysCompleted
        val foundChallenge = tracker.getAllChallenges().firstOrNull { it.id == "c_meditation" }!!
        println("Найден: ${foundChallenge.title}, дней: $forcedDays")
    }

    tracker.printChallengeLog()

    return tracker
}
