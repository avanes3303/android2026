package com.example.myapplication.data.network

import com.example.myapplication.data.network.dto.QuoteDto
import retrofit2.http.GET

interface QuoteApiService {

    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteDto
}
