package com.example.myapplication.domain.model

// =============================================================================
// ЛАБА 1, §4: Интерфейсы
//
// Демонстрация:
//  - Интерфейс с абстрактным методом
//  - Интерфейс с методом по умолчанию (default implementation)
//  - Наследование интерфейсов с переопределением свойства
//  - Класс реализует два и более интерфейса одновременно
// =============================================================================

// --- Интерфейс с абстрактным методом ---
interface Displayable {
    fun displayInfo(): String
}

// --- Интерфейс с методом по умолчанию (default implementation) ---
interface Trackable {
    val trackingId: String

    // Метод по умолчанию — наследники могут переопределить
    fun isActive(): Boolean = true

    fun trackingLabel(): String = "Tracking: $trackingId"
}

// --- Интерфейс наследуется от другого интерфейса с переопределением свойства ---
interface Summarizable : Displayable {
    val summaryTitle: String

    // Переопределяем метод родительского интерфейса, добавляя поведение
    override fun displayInfo(): String = "[$summaryTitle] — переопределено в Summarizable"
}
