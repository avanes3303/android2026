package com.example.myapplication.data.mapper

import com.example.myapplication.data.network.dto.QuoteDto

// =============================================================================
// ЛАБА 5: Маппер DTO → доменная модель
// Отдельный объект для преобразования — DTO не утекает в UI-слой.
// =============================================================================

object QuoteMapper {

    fun toDisplayString(dto: QuoteDto): String =
        "\"${dto.quoteText}\"\n— ${dto.author}"
}
