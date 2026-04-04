package com.attendance.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.attendance.app.data.local.dao.AttendanceDao
import com.attendance.app.data.local.dao.ClassDao
import com.attendance.app.data.local.dao.StudentDao
import com.attendance.app.data.local.entity.AttendanceEntity
import com.attendance.app.data.local.entity.ClassEntity
import com.attendance.app.data.local.entity.StudentEntity

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        const val DATABASE_NAME = "attendance_db"
    }
}
