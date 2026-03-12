package com.example.myapplication.data.mapper

import com.example.myapplication.data.network.dto.QuoteDto

object QuoteMapper {

    fun toDisplayString(dto: QuoteDto): String =
        "\"${dto.quoteText}\"\n— ${dto.author}"
}
