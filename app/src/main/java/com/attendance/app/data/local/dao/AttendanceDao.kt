package com.attendance.app.data.local.dao

import androidx.room.*
import com.attendance.app.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND classId = :classId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: Long, classId: Long): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT DISTINCT date FROM attendance 
        WHERE classId = :classId 
        ORDER BY date DESC
    """)
    fun getSessionDates(classId: Long): Flow<List<String>>

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE classId = :classId AND date = :date AND status = 'PRESENT'
    """)
    suspend fun getPresentCount(classId: Long, date: String): Int

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE classId = :classId AND date = :date
    """)
    suspend fun getTotalCount(classId: Long, date: String): Int

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE classId = :classId AND date = :date AND status = 'ABSENT'
    """)
    suspend fun getAbsentCount(classId: Long, date: String): Int

    @Query("DELETE FROM attendance WHERE classId = :classId AND date = :date")
    suspend fun deleteAttendanceByClassAndDate(classId: Long, date: String)

    @Query("""
        SELECT DISTINCT date FROM attendance 
        WHERE classId = :classId 
        ORDER BY date DESC 
        LIMIT :limit
    """)
    fun getRecentSessions(classId: Long, limit: Int = 10): Flow<List<String>>

    @Query("SELECT * FROM attendance WHERE classId = :classId")
    suspend fun getAllAttendanceForClass(classId: Long): List<AttendanceEntity>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendance(): List<AttendanceEntity>
}
