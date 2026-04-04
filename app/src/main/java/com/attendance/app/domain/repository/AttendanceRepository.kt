package com.attendance.app.domain.repository

import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.SessionSummary
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceRecord>>
    fun getAttendanceByStudent(studentId: Long, classId: Long): Flow<List<AttendanceRecord>>
    suspend fun saveAttendance(records: List<AttendanceRecord>)
    suspend fun getSessionSummary(classId: Long, date: String): SessionSummary
    fun getRecentSessions(classId: Long, limit: Int = 10): Flow<List<String>>
    fun getSessionDates(classId: Long): Flow<List<String>>
    suspend fun getAllAttendanceForClass(classId: Long): List<AttendanceRecord>
    suspend fun getAllAttendance(): List<AttendanceRecord>
}
