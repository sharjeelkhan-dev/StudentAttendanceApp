package com.attendance.app.di

import android.content.Context
import androidx.room.Room
import com.attendance.app.data.local.AppDatabase
import com.attendance.app.data.local.dao.AttendanceDao
import com.attendance.app.data.local.dao.ClassDao
import com.attendance.app.data.local.dao.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideClassDao(database: AppDatabase): ClassDao = database.classDao()

    @Provides
    @Singleton
    fun provideStudentDao(database: AppDatabase): StudentDao = database.studentDao()

    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()
}
