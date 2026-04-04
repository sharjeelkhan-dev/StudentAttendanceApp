package com.attendance.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object TakeAttendance : Screen("take_attendance")
    data object Reports : Screen("reports")
    data object Students : Screen("students")
    data object Classes : Screen("classes")
    data object Settings : Screen("settings")
}
