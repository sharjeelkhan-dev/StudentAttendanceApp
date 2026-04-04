package com.attendance.app.data.repository

import com.attendance.app.data.local.dao.ClassDao
import com.attendance.app.data.local.entity.ClassEntity
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.repository.ClassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClassRepositoryImpl @Inject constructor(
    private val classDao: ClassDao
) : ClassRepository {

    override fun getAllClasses(): Flow<List<ClassModel>> {
        return classDao.getAllClasses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getClassById(classId: Long): ClassModel? {
        return classDao.getClassById(classId)?.toDomain()
    }

    override suspend fun insertClass(classModel: ClassModel): Long {
        return classDao.insertClass(classModel.toEntity())
    }

    override suspend fun updateClass(classModel: ClassModel) {
        classDao.updateClass(classModel.toEntity())
    }

    override suspend fun deleteClass(classModel: ClassModel) {
        classDao.deleteClass(classModel.toEntity())
    }

    private fun ClassEntity.toDomain(): ClassModel {
        return ClassModel(
            id = id,
            name = name,
            section = section,
            createdAt = createdAt
        )
    }

    private fun ClassModel.toEntity(): ClassEntity {
        return ClassEntity(
            id = id,
            name = name,
            section = section,
            createdAt = createdAt
        )
    }
}
