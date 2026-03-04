package com.example.myapplication.data.network

import com.example.myapplication.data.network.dto.QuoteDto
import retrofit2.http.GET

// =============================================================================
// ЛАБА 3: Retrofit-интерфейс сетевого сервиса
//
// Retrofit — библиотека для работы с REST API в Android.
// HTTP-эндпоинты описываются как методы интерфейса с аннотациями (@GET, @POST).
// suspend — функция запускается в корутине (не блокирует главный поток).
// =============================================================================

interface QuoteApiService {

    // GET-запрос к /quotes/random (baseUrl = "https://dummyjson.com/")
    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteDto
}
