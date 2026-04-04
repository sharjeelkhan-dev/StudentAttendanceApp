package com.attendance.app.domain.model

data class AttendanceRecord(
    val id: Long = 0,
    val studentId: Long,
    val classId: Long,
    val date: String,
    val status: AttendanceStatus
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE
}
