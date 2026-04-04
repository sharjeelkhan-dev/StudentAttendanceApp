package com.attendance.app.domain.repository

import com.attendance.app.domain.model.ClassModel
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    fun getAllClasses(): Flow<List<ClassModel>>
    suspend fun getClassById(classId: Long): ClassModel?
    suspend fun insertClass(classModel: ClassModel): Long
    suspend fun updateClass(classModel: ClassModel)
    suspend fun deleteClass(classModel: ClassModel)
}
