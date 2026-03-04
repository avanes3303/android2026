package com.example.myapplication.presentation.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityDetailBinding

// =============================================================================
// ЛАБА 2: DetailActivity — экран деталей челленджа
//
// Демонстрация:
//  - Явный Intent с putExtra для передачи данных (§Intent)
//  - Неявный Intent для шаринга (§Intent)
//  - launchMode="singleTop" в манифесте + onNewIntent (§Back Stack)
//  - Lifecycle logging
//  - View Binding
// =============================================================================

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val TAG = "DetailActivity"
        // Ключи для Intent.putExtra — константы в companion object
        const val EXTRA_EMOJI = "extra_emoji"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_TARGET_DAYS = "extra_target_days"
        const val EXTRA_STREAK_DAYS = "extra_streak_days"
        const val EXTRA_COMPLETION = "extra_completion"

        /**
         * Фабричный метод для создания Intent — удобнее чем вручную
         * собирать extras в каждом месте вызова.
         */
        fun newIntent(
            context: Context,
            emoji: String,
            title: String,
            description: String,
            category: String,
            targetDays: Int,
            streakDays: Int,
            completion: Int
        ): Intent = Intent(context, DetailActivity::class.java).apply {
            putExtra(EXTRA_EMOJI, emoji)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_CATEGORY, category)
            putExtra(EXTRA_TARGET_DAYS, targetDays)
            putExtra(EXTRA_STREAK_DAYS, streakDays)
            putExtra(EXTRA_COMPLETION, completion)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayChallengeDetails()
        setupShareButton()
        setupBackButton()
    }

    // ЛАБА 2: onNewIntent — вызывается при launchMode="singleTop"
    // когда Activity уже на вершине стека и открывается повторно
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent — Activity переиспользована (singleTop)")
        setIntent(intent)
        displayChallengeDetails()
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }

    private fun displayChallengeDetails() {
        // ЛАБА 2: Получение данных из Intent через getStringExtra / getIntExtra
        val emoji = intent.getStringExtra(EXTRA_EMOJI) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
        val targetDays = intent.getIntExtra(EXTRA_TARGET_DAYS, 0)
        val streakDays = intent.getIntExtra(EXTRA_STREAK_DAYS, 0)
        val completion = intent.getIntExtra(EXTRA_COMPLETION, 0)

        binding.tvDetailEmoji.text = emoji
        binding.tvDetailTitle.text = title
        binding.tvDetailDescription.text = description
        binding.tvDetailCategory.text = getString(R.string.detail_category, category)
        binding.tvDetailTarget.text = getString(R.string.detail_target, targetDays)
        binding.tvDetailStreak.text = getString(R.string.detail_streak, streakDays)
        binding.tvDetailCompletion.text = getString(R.string.detail_completion, completion)
        binding.progressDetail.progress = completion
    }

    private fun setupShareButton() {
        binding.btnShare.setOnClickListener {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
            val streak = intent.getIntExtra(EXTRA_STREAK_DAYS, 0)

            // ЛАБА 2: Неявный Intent — открывает системный диалог шаринга
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.detail_share_text, title, streak))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.detail_share)))
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
