package com.attendance.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.preferences.PreferencesManager
import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.SessionSummary
import com.attendance.app.domain.model.Student
import com.attendance.app.domain.repository.AttendanceRepository
import com.attendance.app.domain.repository.ClassRepository
import com.attendance.app.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import javax.inject.Inject

data class StudentReport(
    val student: Student,
    val attendancePercentage: Double,
    val presentCount: Int,
    val totalSessions: Int
)

data class SessionWithRecords(
    val summary: SessionSummary,
    val records: List<AttendanceRecord>,
    val students: List<Student>
)

data class ReportsState(
    val selectedClass: ClassModel? = null,
    val studentReports: List<StudentReport> = emptyList(),
    val sessionDetails: List<SessionWithRecords> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            preferencesManager.selectedClassIdFlow
                .collectLatest { classId ->
                    if (classId != -1L) {
                        val classModel = classRepository.getClassById(classId)
                        _state.update { it.copy(selectedClass = classModel, isLoading = true) }

                        combine(
                            studentRepository.getStudentsByClass(classId),
                            attendanceRepository.getSessionDates(classId)
                        ) { students, dates ->
                                if (dates.isEmpty()) {
                                    val reports = students.map { student ->
                                        StudentReport(
                                            student = student,
                                            attendancePercentage = 0.0,
                                            presentCount = 0,
                                            totalSessions = 0
                                        )
                                    }
                                    flowOf(reports to emptyList<SessionWithRecords>())
                                } else {
                                    val sessionFlows = dates.map { date ->
                                        combine(
                                            attendanceRepository.getSessionSummary(classId, date),
                                            attendanceRepository.getAttendanceByClassAndDate(classId, date)
                                        ) { summary, records ->
                                            SessionWithRecords(summary, records, students)
                                        }
                                    }
                                    
                                    combine(sessionFlows) { sessions ->
                                        val sessionList = sessions.toList()
                                        val reports = students.map { student ->
                                            val studentCreatedDate = try {
                                                Instant.ofEpochMilli(student.createdAt)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate()
                                            } catch (e: Exception) { LocalDate.MIN }

                                            // Only count sessions that happened AFTER or ON the student's creation date
                                            val relevantSessions = sessionList.filter { session ->
                                                try {
                                                    val sessionDate = LocalDate.parse(session.summary.date)
                                                    !sessionDate.isBefore(studentCreatedDate)
                                                } catch (e: Exception) { true }
                                            }

                                            val totalSessions = relevantSessions.size
                                            val presentCount = relevantSessions.count { session ->
                                                session.records.any { it.studentId == student.id && it.status == com.attendance.app.domain.model.AttendanceStatus.PRESENT }
                                            }
                                            val pct = if (totalSessions > 0) (presentCount.toDouble() / totalSessions * 100.0) else 0.0
                                            StudentReport(
                                                student = student,
                                                attendancePercentage = pct,
                                                presentCount = presentCount,
                                                totalSessions = totalSessions
                                            )
                                        }

                                        // Filter session details to only show students who were enrolled on that date
                                        val filteredSessions = sessionList.map { session ->
                                            val sessionDate = try {
                                                LocalDate.parse(session.summary.date)
                                            } catch (e: Exception) { LocalDate.MAX }

                                            val enrolledStudents = students.filter { student ->
                                                val studentCreatedDate = try {
                                                    Instant.ofEpochMilli(student.createdAt)
                                                        .atZone(ZoneId.systemDefault())
                                                        .toLocalDate()
                                                } catch (e: Exception) { LocalDate.MIN }
                                                !sessionDate.isBefore(studentCreatedDate)
                                            }

                                            // Update summary counts based on enrolled students for that day
                                            val recordsForEnrolled = session.records.filter { record ->
                                                enrolledStudents.any { it.id == record.studentId }
                                            }
                                            val presentOnDay = recordsForEnrolled.count { it.status == com.attendance.app.domain.model.AttendanceStatus.PRESENT }

                                            session.copy(
                                                students = enrolledStudents,
                                                summary = session.summary.copy(
                                                    totalStudents = enrolledStudents.size,
                                                    presentCount = presentOnDay,
                                                    absentCount = enrolledStudents.size - presentOnDay
                                                )
                                            )
                                        }

                                        reports to filteredSessions
                                    }
                                }
                        }.flatMapLatest { it }
                        .collect { (reports, sessions) ->
                            _state.update {
                                it.copy(
                                    studentReports = reports,
                                    sessionDetails = sessions.sortedByDescending { s -> s.summary.date },
                                    isLoading = false
                                )
                            }
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, selectedClass = null, studentReports = emptyList(), sessionDetails = emptyList()) }
                    }
                }
        }
    }
}
