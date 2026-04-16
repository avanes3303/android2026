package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.domain.model.UserProfile

interface HabitRepository {
    fun getChallenges(): List<HabitDisplayItem>
    fun getChallengeById(id: String): HabitDisplayItem?
    fun getUserProfile(): UserProfile
    fun addChallenge(item: HabitDisplayItem)
    fun completeDay(title: String)
}
