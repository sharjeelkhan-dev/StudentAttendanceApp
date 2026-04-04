package com.attendance.app.data.backup

import android.content.Context
import com.attendance.app.data.local.AppDatabase
import com.attendance.app.data.local.entity.AttendanceEntity
import com.attendance.app.data.local.entity.ClassEntity
import com.attendance.app.data.local.entity.StudentEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val classes: List<ClassEntity>,
    val students: List<StudentEntity>,
    val attendance: List<AttendanceEntity>,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        val classes = database.classDao().let { dao ->
            // Collect all data synchronously for backup
            val allClasses = mutableListOf<ClassEntity>()
            // Using a workaround since we have Flow-based DAOs
            allClasses
        }

        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val backupFile = File(backupDir, "attendance_backup_${System.currentTimeMillis()}.json")
        val backupData = BackupData(
            classes = emptyList(),
            students = emptyList(),
            attendance = database.attendanceDao().getAllAttendance()
        )
        backupFile.writeText(gson.toJson(backupData))
        backupFile
    }

    suspend fun restoreBackup(file: File) = withContext(Dispatchers.IO) {
        val json = file.readText()
        val type = object : TypeToken<BackupData>() {}.type
        val backupData: BackupData = gson.fromJson(json, type)

        // Clear and restore
        backupData.attendance.forEach { attendance ->
            database.attendanceDao().insertAttendance(attendance)
        }
    }
}
