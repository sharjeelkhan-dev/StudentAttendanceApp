package com.attendance.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.preferences.PreferencesManager
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.SessionSummary
import com.attendance.app.domain.repository.AttendanceRepository
import com.attendance.app.domain.repository.ClassRepository
import com.attendance.app.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeState(
    val selectedClass: ClassModel? = null,
    val classes: List<ClassModel> = emptyList(),
    val totalStudents: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val recentSessions: List<SessionSummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeHomeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHomeData() {
        // 1. Combine all classes and selected ID
        val classInfoFlow = combine(
            classRepository.getAllClasses(),
            preferencesManager.selectedClassIdFlow
        ) { classes, selectedId ->
            val selected = classes.find { it.id == selectedId } ?: classes.firstOrNull()
            
            // Auto-select first class if none is selected
            if (selected != null && selectedId == -1L) {
                preferencesManager.setSelectedClassId(selected.id)
            }
            
            Pair(classes, selected)
        }

        // 2. Use flatMapLatest to switch flows whenever the selected class changes
        classInfoFlow.flatMapLatest { (classes, selectedClass) ->
            if (selectedClass != null) {
                // Combine student updates and attendance updates
                combine(
                    studentRepository.getStudentsByClass(selectedClass.id),
                    attendanceRepository.getRecentSessions(selectedClass.id, 10),
                    // We also need to trigger refresh when attendance is saved for today
                    attendanceRepository.getAttendanceByClassAndDate(selectedClass.id, LocalDate.now().toString())
                ) { students, recentDates, _ ->
                    val today = LocalDate.now().toString()
                    val todaySummary = attendanceRepository.getSessionSummary(selectedClass.id, today)
                    
                    val sessions = recentDates.map { date ->
                        attendanceRepository.getSessionSummary(selectedClass.id, date)
                    }
                    
                    HomeState(
                        classes = classes,
                        selectedClass = selectedClass,
                        totalStudents = students.size,
                        presentToday = todaySummary.presentCount,
                        absentToday = todaySummary.absentCount,
                        recentSessions = sessions,
                        isLoading = false
                    )
                }
            } else {
                flowOf(HomeState(classes = classes, isLoading = false))
            }
        }
        .onEach { newState ->
            _state.value = newState
        }
        .catch { e ->
            _state.update { it.copy(isLoading = false) }
            e.printStackTrace()
        }
        .launchIn(viewModelScope)
    }

    fun selectClass(classModel: ClassModel) {
        viewModelScope.launch {
            preferencesManager.setSelectedClassId(classModel.id)
        }
    }
}
