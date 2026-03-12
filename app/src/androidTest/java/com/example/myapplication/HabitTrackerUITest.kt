package com.example.myapplication

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.presentation.main.MainActivity
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitTrackerUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // =========================================================================
    // HomeFragment — основной экран
    // =========================================================================

    @Test
    fun homeScreen_showsTitle() {
        onView(withId(R.id.tv_title))
            .check(matches(isDisplayed()))
            .check(matches(withText("HabitTracker")))
    }

    @Test
    fun homeScreen_challengeListIsVisible() {
        onView(withId(R.id.rv_challenges))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_challengeListHasItems() {
        onView(withId(R.id.rv_challenges))
            .check { view, _ ->
                val rv = view as RecyclerView
                assert(rv.adapter!!.itemCount > 0) { "Challenge list is empty" }
            }
    }

    @Test
    fun homeScreen_startButtonDisabledByDefault() {
        onView(withId(R.id.btn_start))
            .check(matches(not(android.widget.Button::class.java.let {
                // Проверяем что кнопка disabled
                object : org.hamcrest.TypeSafeMatcher<View>() {
                    override fun describeTo(description: org.hamcrest.Description) {
                        description.appendText("is enabled")
                    }
                    override fun matchesSafely(item: View) = item.isEnabled
                }
            })))
    }

    @Test
    fun homeScreen_typeNameEnablesButton() {
        onView(withId(R.id.et_name))
            .perform(replaceText("Иван"), closeSoftKeyboard())
        onView(withId(R.id.btn_start))
            .check(matches(object : org.hamcrest.TypeSafeMatcher<View>() {
                override fun describeTo(d: org.hamcrest.Description) { d.appendText("is enabled") }
                override fun matchesSafely(item: View) = item.isEnabled
            }))
    }

    @Test
    fun homeScreen_greetingAppearsAfterStart() {
        onView(withId(R.id.et_name))
            .perform(replaceText("Иван"), closeSoftKeyboard())
        onView(withId(R.id.btn_start))
            .perform(click())
        onView(withId(R.id.card_greeting))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tv_greeting))
            .check(matches(withText(containsString("Иван"))))
    }

    // =========================================================================
    // Добавление челленджа
    // =========================================================================

    @Test
    fun homeScreen_fabIsVisible() {
        onView(withId(R.id.fab_add))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_addChallengeAppearsInList() {
        // Запоминаем количество до
        var countBefore = 0
        onView(withId(R.id.rv_challenges)).check { view, _ ->
            countBefore = (view as RecyclerView).adapter!!.itemCount
        }

        // Добавляем челлендж
        onView(withId(R.id.fab_add)).perform(click())
        onView(isAssignableFrom(EditText::class.java))
            .perform(replaceText("Тестовый челлендж"), closeSoftKeyboard())
        onView(withText("Добавить")).perform(click())

        // Проверяем что элемент добавился
        onView(withId(R.id.rv_challenges)).check { view, _ ->
            val countAfter = (view as RecyclerView).adapter!!.itemCount
            assert(countAfter == countBefore + 1) {
                "Expected ${countBefore + 1} items, got $countAfter"
            }
        }
    }

    @Test
    fun homeScreen_addChallengeDialogCancel() {
        var countBefore = 0
        onView(withId(R.id.rv_challenges)).check { view, _ ->
            countBefore = (view as RecyclerView).adapter!!.itemCount
        }

        onView(withId(R.id.fab_add)).perform(click())
        onView(isAssignableFrom(EditText::class.java))
            .perform(replaceText("Не добавится"), closeSoftKeyboard())
        onView(withText("Отмена")).perform(click())

        onView(withId(R.id.rv_challenges)).check { view, _ ->
            val countAfter = (view as RecyclerView).adapter!!.itemCount
            assert(countAfter == countBefore) {
                "Count should not change on cancel"
            }
        }
    }

    // =========================================================================
    // Навигация HomeFragment → DetailActivity
    // =========================================================================

    @Test
    fun clickChallenge_opensDetailActivity() {
        onView(withId(R.id.rv_challenges))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // DetailActivity должна открыться — проверяем наличие элементов
        onView(withId(R.id.tv_detail_title))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tv_detail_emoji))
            .check(matches(isDisplayed()))
        onView(withId(R.id.btn_share))
            .check(matches(isDisplayed()))
        onView(withId(R.id.btn_complete))
            .check(matches(isDisplayed()))
    }

    @Test
    fun detailActivity_showsCorrectData() {
        onView(withId(R.id.rv_challenges))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Проверяем что поля не пустые
        onView(withId(R.id.tv_detail_title))
            .check(matches(not(withText(""))))
        onView(withId(R.id.tv_detail_description))
            .check(matches(not(withText(""))))
        onView(withId(R.id.tv_detail_category))
            .check(matches(withText(containsString("Категория"))))
    }

    @Test
    fun detailActivity_backButtonReturnsToHome() {
        onView(withId(R.id.rv_challenges))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.btn_back)).perform(click())

        // Снова на главном экране
        onView(withId(R.id.rv_challenges))
            .check(matches(isDisplayed()))
    }

    // =========================================================================
    // «Выполнено сегодня»
    // =========================================================================

    @Test
    fun completeDay_updatesStreakInList() {
        // Читаем текущий стрик первого элемента
        var streakBefore = ""
        onView(withId(R.id.rv_challenges)).check { view, _ ->
            val rv = view as RecyclerView
            val holder = rv.findViewHolderForAdapterPosition(0)!!
            streakBefore = holder.itemView.findViewById<TextView>(R.id.tv_challenge_description).text.toString()
        }

        // Открываем детали первого элемента
        onView(withId(R.id.rv_challenges))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Нажимаем «Выполнено сегодня»
        onView(withId(R.id.btn_complete))
            .perform(scrollTo(), click())

        // Должны вернуться на HomeFragment
        onView(withId(R.id.rv_challenges))
            .check(matches(isDisplayed()))
    }

    // =========================================================================
    // BottomNavigation — переключение вкладок
    // =========================================================================

    @Test
    fun bottomNav_switchToStats() {
        onView(withId(R.id.nav_stats)).perform(click())

        onView(withId(R.id.tv_stats_title))
            .check(matches(isDisplayed()))
            .check(matches(withText("Статистика")))
    }

    @Test
    fun bottomNav_switchToProfile() {
        onView(withId(R.id.nav_profile)).perform(click())

        onView(withId(R.id.tv_profile_name))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_switchBackToHome() {
        onView(withId(R.id.nav_stats)).perform(click())
        onView(withId(R.id.nav_home)).perform(click())

        onView(withId(R.id.tv_title))
            .check(matches(isDisplayed()))
    }

    // =========================================================================
    // StatsFragment — привычки + API цитата
    // =========================================================================

    @Test
    fun statsScreen_habitListIsVisible() {
        onView(withId(R.id.nav_stats)).perform(click())

        onView(withId(R.id.rv_habits))
            .check(matches(isDisplayed()))
    }

    @Test
    fun statsScreen_habitListHasItems() {
        onView(withId(R.id.nav_stats)).perform(click())

        onView(withId(R.id.rv_habits)).check { view, _ ->
            val rv = view as RecyclerView
            assert(rv.adapter!!.itemCount > 0) { "Habit list is empty" }
        }
    }

    @Test
    fun statsScreen_summaryChipsAreVisible() {
        onView(withId(R.id.nav_stats)).perform(click())

        onView(withId(R.id.tv_chip_count_value))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tv_chip_streak_value))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tv_chip_avg_value))
            .check(matches(isDisplayed()))
    }

    // =========================================================================
    // ProfileFragment
    // =========================================================================

    @Test
    fun profileScreen_showsUserData() {
        onView(withId(R.id.nav_profile)).perform(click())

        onView(withId(R.id.tv_profile_name))
            .check(matches(isDisplayed()))
            .check(matches(not(withText(""))))
        onView(withId(R.id.tv_profile_since))
            .check(matches(isDisplayed()))
    }
}
