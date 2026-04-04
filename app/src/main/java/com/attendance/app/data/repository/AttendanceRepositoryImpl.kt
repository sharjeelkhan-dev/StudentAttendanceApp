package com.attendance.app.data.repository

import com.attendance.app.data.local.dao.AttendanceDao
import com.attendance.app.data.local.entity.AttendanceEntity
import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.domain.model.SessionSummary
import com.attendance.app.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao
) : AttendanceRepository {

    override fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceByClassAndDate(classId, date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAttendanceByStudent(studentId: Long, classId: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceByStudent(studentId, classId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveAttendance(records: List<AttendanceRecord>) {
        val entities = records.map { it.toEntity() }
        attendanceDao.insertAllAttendance(entities)
    }

    override suspend fun getSessionSummary(classId: Long, date: String): SessionSummary {
        val totalCount = attendanceDao.getTotalCount(classId, date)
        val presentCount = attendanceDao.getPresentCount(classId, date)
        val absentCount = attendanceDao.getAbsentCount(classId, date)
        return SessionSummary(
            date = date,
            totalStudents = totalCount,
            presentCount = presentCount,
            absentCount = absentCount
        )
    }

    override fun getRecentSessions(classId: Long, limit: Int): Flow<List<String>> {
        return attendanceDao.getRecentSessions(classId, limit)
    }

    override fun getSessionDates(classId: Long): Flow<List<String>> {
        return attendanceDao.getSessionDates(classId)
    }

    override suspend fun getAllAttendanceForClass(classId: Long): List<AttendanceRecord> {
        return attendanceDao.getAllAttendanceForClass(classId).map { it.toDomain() }
    }

    override suspend fun getAllAttendance(): List<AttendanceRecord> {
        return attendanceDao.getAllAttendance().map { it.toDomain() }
    }

    private fun AttendanceEntity.toDomain(): AttendanceRecord {
        return AttendanceRecord(
            id = id,
            studentId = studentId,
            classId = classId,
            date = date,
            status = AttendanceStatus.valueOf(status)
        )
    }

    private fun AttendanceRecord.toEntity(): AttendanceEntity {
        return AttendanceEntity(
            id = id,
            studentId = studentId,
            classId = classId,
            date = date,
            status = status.name
        )
    }
}
