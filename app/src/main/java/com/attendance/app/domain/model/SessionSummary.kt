package com.attendance.app.domain.model

data class SessionSummary(
    val date: String,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int
) {
    val percentage: Int
        get() = if (totalStudents == 0) 0 else ((presentCount * 100.0) / totalStudents).toInt()
}
