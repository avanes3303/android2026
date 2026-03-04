package com.example.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

// =============================================================================
// ЛАБА 3: DTO — Data Transfer Object для ответа API
// @SerializedName маппит JSON-ключи на поля Kotlin data class
// =============================================================================

data class QuoteDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("quote")
    val quoteText: String,       // имя поля отличается от JSON-ключа — демо @SerializedName

    @SerializedName("author")
    val author: String
)
