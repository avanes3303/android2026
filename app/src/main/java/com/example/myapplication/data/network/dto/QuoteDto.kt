package com.example.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class QuoteDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("quote")
    val quoteText: String,

    @SerializedName("author")
    val author: String
)
