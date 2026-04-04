package com.attendance.app.data.repository

import com.attendance.app.data.local.dao.StudentDao
import com.attendance.app.data.local.entity.StudentEntity
import com.attendance.app.domain.model.Student
import com.attendance.app.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao
) : StudentRepository {

    override fun getStudentsByClass(classId: Long): Flow<List<Student>> {
        return studentDao.getStudentsByClass(classId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStudentById(studentId: Long): Student? {
        return studentDao.getStudentById(studentId)?.toDomain()
    }

    override suspend fun insertStudent(student: Student): Long {
        return studentDao.insertStudent(student.toEntity())
    }

    override suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student.toEntity())
    }

    override suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student.toEntity())
    }

    override suspend fun getAttendancePercentage(studentId: Long, classId: Long): Double {
        return studentDao.getAttendancePercentage(studentId, classId)
    }

    override fun searchStudents(classId: Long, query: String): Flow<List<Student>> {
        return studentDao.searchStudents(classId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun StudentEntity.toDomain(): Student {
        return Student(
            id = id,
            fullName = fullName,
            rollNumber = rollNumber,
            classId = classId,
            createdAt = createdAt
        )
    }

    private fun Student.toEntity(): StudentEntity {
        return StudentEntity(
            id = id,
            fullName = fullName,
            rollNumber = rollNumber,
            classId = classId,
            createdAt = createdAt
        )
    }
}
