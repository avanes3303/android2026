package com.example.myapplication.domain.model

interface Displayable {
    fun displayInfo(): String
}

interface Trackable {
    val trackingId: String

    // Метод по умолчанию — наследники могут переопределить
    fun isActive(): Boolean = true

    fun trackingLabel(): String = "Tracking: $trackingId"
}

interface Summarizable : Displayable {
    val summaryTitle: String

    override fun displayInfo(): String = "[$summaryTitle] — переопределено в Summarizable"
}
