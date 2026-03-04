package com.example.myapplication.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.JsonDataProvider
import com.example.myapplication.data.repository.HabitRepositoryImpl
import com.example.myapplication.data.repository.QuoteRepositoryImpl
import com.example.myapplication.domain.repository.HabitRepository
import com.example.myapplication.domain.repository.QuoteRepository
import com.example.myapplication.presentation.home.HomeViewModel
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.presentation.stats.StatsViewModel

// =============================================================================
// ЛАБА 5: DI через конструктор (без фреймворка)
//
// ViewModelFactory передаёт зависимости (Repository) в ViewModel.
// Зависимости не создаются внутри ViewModel — передаются снаружи.
//
// В будущем заменяется на Hilt/Koin для автоматического DI.
// =============================================================================

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    // Используем applicationContext чтобы не держать ссылку на Activity (утечка памяти)
    private val appContext = context.applicationContext

    private val jsonDataProvider by lazy { JsonDataProvider(appContext) }
    private val habitRepository: HabitRepository by lazy { HabitRepositoryImpl(jsonDataProvider) }
    private val quoteRepository: QuoteRepository by lazy { QuoteRepositoryImpl() }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(habitRepository) as T
        modelClass.isAssignableFrom(StatsViewModel::class.java) ->
            StatsViewModel(habitRepository, quoteRepository) as T
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(habitRepository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
