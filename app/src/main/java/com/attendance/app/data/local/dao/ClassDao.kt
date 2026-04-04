package com.attendance.app.data.local.dao

import androidx.room.*
import com.attendance.app.data.local.entity.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)

    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :classId")
    suspend fun getClassById(classId: Long): ClassEntity?

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    suspend fun getStudentCount(classId: Long): Int
}
