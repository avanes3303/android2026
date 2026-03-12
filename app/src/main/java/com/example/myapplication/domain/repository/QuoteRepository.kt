package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ApiResult

interface QuoteRepository {
    suspend fun getMotivationalQuote(): ApiResult<String>
}
