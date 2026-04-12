package com.attendance.app.presentation.students

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.Student
import com.attendance.app.domain.repository.AttendanceRepository
import com.attendance.app.domain.repository.ClassRepository
import com.attendance.app.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDetailState(
    val student: Student? = null,
    val classModel: ClassModel? = null,
    val attendanceLogs: List<AttendanceRecord> = emptyList(),
    val attendancePercentage: Double = 0.0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
open class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val classRepository: ClassRepository,
    private val attendanceRepository: AttendanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: Long = checkNotNull(savedStateHandle["studentId"])
    private val classId: Long = checkNotNull(savedStateHandle["classId"])

    private val _state = MutableStateFlow(StudentDetailState())
    val state: StateFlow<StudentDetailState> = _state.asStateFlow()

    init {
        loadStudentDetails()
    }

    private fun loadStudentDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val student = studentRepository.getStudentById(studentId)
                val classModel = classRepository.getClassById(classId)
                
                if (student != null) {
                    _state.update { it.copy(student = student, classModel = classModel) }
                    
                    // Combine percentage and logs
                    val percentage = studentRepository.getAttendancePercentage(studentId, classId)
                    
                    attendanceRepository.getAttendanceByStudent(studentId, classId)
                        .collect { logs ->
                            val present = logs.count { it.status == com.attendance.app.domain.model.AttendanceStatus.PRESENT }
                            val absent = logs.count { it.status == com.attendance.app.domain.model.AttendanceStatus.ABSENT }
                            
                            _state.update { 
                                it.copy(
                                    attendanceLogs = logs.sortedByDescending { it.date },
                                    attendancePercentage = percentage,
                                    presentCount = present,
                                    absentCount = absent,
                                    isLoading = false
                                )
                            }
                        }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Student not found") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}
