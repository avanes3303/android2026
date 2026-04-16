package com.example.myapplication.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.JsonDataProvider
import com.example.myapplication.data.repository.HabitRepositoryImpl
import com.example.myapplication.data.repository.QuoteRepositoryImpl
import com.example.myapplication.data.sdui.SduiRepositoryImpl
import com.example.myapplication.domain.repository.HabitRepository
import com.example.myapplication.domain.repository.QuoteRepository
import com.example.myapplication.domain.sdui.SduiRepository
import com.example.myapplication.presentation.home.HomeViewModel
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.presentation.sdui.SduiViewModel
import com.example.myapplication.presentation.stats.StatsViewModel

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val habitRepository: HabitRepository
    private val quoteRepository: QuoteRepository
    private val sduiRepository: SduiRepository

    init {
        val appContext = context.applicationContext
        synchronized(lock) {
            if (_habitRepository == null) {
                val jsonDataProvider = JsonDataProvider(appContext)
                _habitRepository = HabitRepositoryImpl(jsonDataProvider)
                _quoteRepository = QuoteRepositoryImpl()
                _sduiRepository = SduiRepositoryImpl(appContext)
            }
        }
        habitRepository = _habitRepository!!
        quoteRepository = _quoteRepository!!
        sduiRepository = _sduiRepository!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(habitRepository) as T
        modelClass.isAssignableFrom(StatsViewModel::class.java) ->
            StatsViewModel(habitRepository, quoteRepository) as T
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(habitRepository) as T
        modelClass.isAssignableFrom(SduiViewModel::class.java) ->
            SduiViewModel(sduiRepository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }

    companion object {
        private val lock = Any()
        private var _habitRepository: HabitRepository? = null
        private var _quoteRepository: QuoteRepository? = null
        private var _sduiRepository: SduiRepository? = null
    }
}
