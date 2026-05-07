package com.attendance.app.data.repository

import com.attendance.app.data.local.dao.AttendanceDao
import com.attendance.app.data.local.dao.ClassDao
import com.attendance.app.data.local.dao.StudentDao
import com.attendance.app.domain.repository.SyncRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val classDao: ClassDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) : SyncRepository {

    override suspend fun uploadDataToCloud(): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))
        
        return try {
            val batch = firestore.batch()
            val userDoc = firestore.collection("users").document(userId)

            // 1. Sync Classes
            val classes = classDao.getAllClassesOnce()
            classes.forEach { classEntity ->
                val classRef = userDoc.collection("classes").document(classEntity.id.toString())
                batch.set(classRef, classEntity, SetOptions.merge())
            }

            // 2. Sync Students
            val students = studentDao.getAllStudentsOnce()
            students.forEach { studentEntity ->
                // Calculate percentage before uploading
                val percentage = studentDao.getAttendancePercentage(studentEntity.id, studentEntity.classId)
                
                val studentData = mapOf(
                    "id" to studentEntity.id,
                    "fullName" to studentEntity.fullName,
                    "rollNumber" to studentEntity.rollNumber,
                    "classId" to studentEntity.classId,
                    "enrollmentDate" to studentEntity.createdAt, // createdAt represents enrollment
                    "attendancePercentage" to percentage
                )
                
                val studentRef = userDoc.collection("students").document(studentEntity.id.toString())
                batch.set(studentRef, studentData, SetOptions.merge())
            }

            // 3. Sync Attendance
            val attendance = attendanceDao.getAllAttendance()
            attendance.forEach { attendanceEntity ->
                val attendanceId = "${attendanceEntity.classId}_${attendanceEntity.studentId}_${attendanceEntity.date}"
                val attendanceRef = userDoc.collection("attendance").document(attendanceId)
                batch.set(attendanceRef, attendanceEntity, SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadDataFromCloud(): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))
        
        return try {
            val userDoc = firestore.collection("users").document(userId)

            // 1. Download Classes
            val classesSnapshot = userDoc.collection("classes").get().await()
            val classes = classesSnapshot.toObjects(com.attendance.app.data.local.entity.ClassEntity::class.java)
            classes.forEach { classDao.insertClass(it) }

            // 2. Download Students
            val studentsSnapshot = userDoc.collection("students").get().await()
            val students = studentsSnapshot.toObjects(com.attendance.app.data.local.entity.StudentEntity::class.java)
            studentDao.insertAllStudents(students)

            // 3. Download Attendance
            val attendanceSnapshot = userDoc.collection("attendance").get().await()
            val attendance = attendanceSnapshot.toObjects(com.attendance.app.data.local.entity.AttendanceEntity::class.java)
            attendanceDao.insertAllAttendance(attendance)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeCloudChanges(): Flow<Unit> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close()
            return@callbackFlow
        }

        val userDoc = firestore.collection("users").document(userId)

        // Listen for Classes changes
        val classesListener = userDoc.collection("classes").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val classObj = change.document.toObject(com.attendance.app.data.local.entity.ClassEntity::class.java)
                launch {
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> classDao.insertClass(classObj)
                        DocumentChange.Type.REMOVED -> classDao.deleteClassById(classObj.id)
                    }
                    trySend(Unit)
                }
            }
        }

        // Listen for Students changes
        val studentsListener = userDoc.collection("students").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val studentObj = change.document.toObject(com.attendance.app.data.local.entity.StudentEntity::class.java)
                launch {
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> studentDao.insertStudent(studentObj)
                        DocumentChange.Type.REMOVED -> studentDao.deleteStudentById(studentObj.id)
                    }
                    trySend(Unit)
                }
            }
        }

        // Listen for Attendance changes
        val attendanceListener = userDoc.collection("attendance").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val attendanceObj = change.document.toObject(com.attendance.app.data.local.entity.AttendanceEntity::class.java)
                launch {
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> attendanceDao.insertAttendance(attendanceObj)
                        DocumentChange.Type.REMOVED -> attendanceDao.deleteSpecificAttendance(
                            attendanceObj.classId, 
                            attendanceObj.studentId, 
                            attendanceObj.date
                        )
                    }
                    trySend(Unit)
                }
            }
        }

        awaitClose {
            classesListener.remove()
            studentsListener.remove()
            attendanceListener.remove()
        }
    }
}
