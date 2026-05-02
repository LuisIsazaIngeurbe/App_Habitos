package com.luisisaza.habitos.presentation.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val PIN_LOCK = "pin_lock"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val BIOMETRIC_SETUP = "biometric_setup"

    const val HOME = "home"
    const val HABITS = "habits"
    const val DAILY_REVIEW = "daily_review"
    const val REPORTS = "reports"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    const val HABIT_FORM = "habit_form?habitId={habitId}"
    const val BAD_HABIT_FORM = "bad_habit_form?habitId={habitId}"
    const val HABIT_DETAIL = "habit_detail/{habitId}"

    fun habitForm(habitId: Long? = null) =
        if (habitId != null) "habit_form?habitId=$habitId" else "habit_form"

    fun badHabitForm(habitId: Long? = null) =
        if (habitId != null) "bad_habit_form?habitId=$habitId" else "bad_habit_form"

    fun habitDetail(habitId: Long) = "habit_detail/$habitId"
}
