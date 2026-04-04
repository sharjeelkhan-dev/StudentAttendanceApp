package com.attendance.app.domain.model

data class Student(
    val id: Long = 0,
    val fullName: String,
    val rollNumber: String,
    val classId: Long,
    val attendancePercentage: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val initials: String
        get() = fullName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
}
