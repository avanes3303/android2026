package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ApiResult

// =============================================================================
// ЛАБА 5, §5.2: Repository — ИНТЕРФЕЙС в domain-слое
// Реализация лежит в data/repository/QuoteRepositoryImpl.kt
// =============================================================================

interface QuoteRepository {
    suspend fun getMotivationalQuote(): ApiResult<String>
}
