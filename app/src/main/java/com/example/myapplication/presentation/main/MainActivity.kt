package com.example.myapplication.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.presentation.home.HomeFragment
import com.example.myapplication.presentation.profile.ProfileFragment
import com.example.myapplication.presentation.stats.StatsFragment

// =============================================================================
// ЛАБА 2: MainActivity — точка входа в приложение
//
// Single Activity + Fragments паттерн:
//  - BottomNavigationView переключает фрагменты
//  - Lifecycle залогирован (видно в Logcat по тегу "MainActivity")
//  - View Binding вместо findViewById
//  - onSaveInstanceState сохраняет выбранную вкладку при повороте
//
// ЛАБА 2 §Context: используется Activity Context для FragmentManager
// (корректно — фрагменты привязаны к Activity, а не к синглтону)
// =============================================================================

class MainActivity : AppCompatActivity() {

    // View Binding — безопасный доступ к View без findViewById (Л2, Л4)
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG = "MainActivity"
        private const val KEY_SELECTED_TAB = "selected_tab_id"
    }

    // -------------------------------------------------------------------------
    // ЛАБА 2: Activity Lifecycle — каждый метод залогирован
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Восстанавливаем выбранную вкладку после поворота экрана
        val selectedId = savedInstanceState?.getInt(KEY_SELECTED_TAB, R.id.nav_home) ?: R.id.nav_home
        binding.bottomNavigation.selectedItemId = selectedId
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    // ЛАБА 2: Сохранение состояния при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB, binding.bottomNavigation.selectedItemId)
        Log.d(TAG, "onSaveInstanceState: tab=${binding.bottomNavigation.selectedItemId}")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "onRestoreInstanceState")
    }

    // -------------------------------------------------------------------------
    // Navigation setup
    // -------------------------------------------------------------------------

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_stats -> StatsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }

            // ЛАБА 2: Fragment добавляется программно через FragmentManager
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            true
        }
    }
}
