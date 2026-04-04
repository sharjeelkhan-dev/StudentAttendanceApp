package com.attendance.app.domain.repository

import com.attendance.app.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getStudentsByClass(classId: Long): Flow<List<Student>>
    suspend fun getStudentById(studentId: Long): Student?
    suspend fun insertStudent(student: Student): Long
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(student: Student)
    suspend fun getAttendancePercentage(studentId: Long, classId: Long): Double
    fun searchStudents(classId: Long, query: String): Flow<List<Student>>
}
