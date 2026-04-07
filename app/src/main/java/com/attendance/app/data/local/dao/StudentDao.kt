package com.attendance.app.data.local.dao

import androidx.room.*
import com.attendance.app.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNumber ASC")
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Long): StudentEntity?

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    fun getStudentCountByClass(classId: Long): Flow<Int>

    @Query("""
        SELECT 
            CASE WHEN COUNT(*) = 0 THEN 0.0
            ELSE (SUM(CASE WHEN status = 'PRESENT' THEN 1.0 ELSE 0.0 END) * 100.0 / COUNT(*))
            END
        FROM attendance 
        WHERE studentId = :studentId AND classId = :classId
    """)
    suspend fun getAttendancePercentage(studentId: Long, classId: Long): Double

    @Query("SELECT * FROM students WHERE classId = :classId AND (fullName LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%')")
    fun searchStudents(classId: Long, query: String): Flow<List<StudentEntity>>
}
