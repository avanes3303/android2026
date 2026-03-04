package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.QuoteMapper
import com.example.myapplication.data.network.QuoteApiService
import com.example.myapplication.domain.model.ApiResult
import com.example.myapplication.domain.repository.QuoteRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// =============================================================================
// ЛАБА 3 + ЛАБА 5: Repository — реализация сетевого слоя
//
// Паттерн Repository изолирует источник данных от остального кода.
// ViewModel не знает, откуда пришли данные — из сети или кэша.
//
// Многопоточность:
//   Retrofit + suspend автоматически переключается на Dispatchers.IO.
//   Мы НЕ блокируем главный поток (Main Thread).
// =============================================================================

class QuoteRepositoryImpl : QuoteRepository {

    // lazy — объект создаётся только при первом обращении (отложенная инициализация)
    private val api: QuoteApiService by lazy { buildApi() }

    override suspend fun getMotivationalQuote(): ApiResult<String> {
        return try {
            val dto = api.getRandomQuote()
            // Маппинг DTO → строка через отдельный маппер (Л5)
            ApiResult.Success(QuoteMapper.toDisplayString(dto))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка сети")
        }
    }

    private fun buildApi(): QuoteApiService {
        // Логирование HTTP-запросов (видно в Logcat)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApiService::class.java)
    }
}
