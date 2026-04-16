package com.example.myapplication.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.HabitRepository

class ProfileViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    init {
        _profile.value = repository.getUserProfile()
    }
}
