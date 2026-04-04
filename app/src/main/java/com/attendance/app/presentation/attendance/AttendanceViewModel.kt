package com.attendance.app.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.preferences.PreferencesManager
import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.Student
import com.attendance.app.domain.repository.AttendanceRepository
import com.attendance.app.domain.repository.ClassRepository
import com.attendance.app.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StudentAttendanceState(
    val student: Student,
    val status: AttendanceStatus = AttendanceStatus.PRESENT
)

data class AttendanceState(
    val selectedClass: ClassModel? = null,
    val date: String = LocalDate.now().toString(),
    val students: List<StudentAttendanceState> = emptyList(),
    val searchQuery: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class AttendanceEvent {
    data class ToggleStatus(val studentId: Long, val status: AttendanceStatus) : AttendanceEvent()
    data object MarkAllPresent : AttendanceEvent()
    data object MarkAllAbsent : AttendanceEvent()
    data class SearchQueryChanged(val query: String) : AttendanceEvent()
    data object SaveAttendance : AttendanceEvent()
    data object ClearError : AttendanceEvent()
    data object ClearSuccess : AttendanceEvent()
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceState())
    val state: StateFlow<AttendanceState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            preferencesManager.selectedClassIdFlow.collect { classId ->
                if (classId != -1L) {
                    val classModel = classRepository.getClassById(classId)
                    _state.update { it.copy(selectedClass = classModel) }

                    studentRepository.getStudentsByClass(classId).collect { students ->
                        // Check if attendance already exists for today
                        val today = LocalDate.now().toString()
                        attendanceRepository.getAttendanceByClassAndDate(classId, today)
                            .collect { existingRecords ->
                                val studentStates = students.map { student ->
                                    val existing = existingRecords.find { it.studentId == student.id }
                                    StudentAttendanceState(
                                        student = student,
                                        status = existing?.status ?: AttendanceStatus.PRESENT
                                    )
                                }
                                _state.update {
                                    it.copy(
                                        students = studentStates,
                                        presentCount = studentStates.count { s -> s.status == AttendanceStatus.PRESENT },
                                        absentCount = studentStates.count { s -> s.status == AttendanceStatus.ABSENT },
                                        isLoading = false
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    fun onEvent(event: AttendanceEvent) {
        when (event) {
            is AttendanceEvent.ToggleStatus -> {
                val updated = _state.value.students.map { s ->
                    if (s.student.id == event.studentId) s.copy(status = event.status) else s
                }
                _state.update {
                    it.copy(
                        students = updated,
                        presentCount = updated.count { s -> s.status == AttendanceStatus.PRESENT },
                        absentCount = updated.count { s -> s.status == AttendanceStatus.ABSENT }
                    )
                }
            }
            is AttendanceEvent.MarkAllPresent -> {
                val updated = _state.value.students.map { it.copy(status = AttendanceStatus.PRESENT) }
                _state.update {
                    it.copy(
                        students = updated,
                        presentCount = updated.size,
                        absentCount = 0
                    )
                }
            }
            is AttendanceEvent.MarkAllAbsent -> {
                val updated = _state.value.students.map { it.copy(status = AttendanceStatus.ABSENT) }
                _state.update {
                    it.copy(
                        students = updated,
                        presentCount = 0,
                        absentCount = updated.size
                    )
                }
            }
            is AttendanceEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is AttendanceEvent.SaveAttendance -> saveAttendance()
            is AttendanceEvent.ClearError -> _state.update { it.copy(error = null) }
            is AttendanceEvent.ClearSuccess -> _state.update { it.copy(successMessage = null) }
        }
    }

    private fun saveAttendance() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isSaving = true) }
                val classId = _state.value.selectedClass?.id ?: return@launch
                val records = _state.value.students.map { s ->
                    AttendanceRecord(
                        studentId = s.student.id,
                        classId = classId,
                        date = _state.value.date,
                        status = s.status
                    )
                }
                attendanceRepository.saveAttendance(records)
                _state.update { it.copy(isSaving = false, isSaved = true, successMessage = "Attendance saved successfully!") }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = "Failed to save attendance: ${e.message}") }
            }
        }
    }
}
