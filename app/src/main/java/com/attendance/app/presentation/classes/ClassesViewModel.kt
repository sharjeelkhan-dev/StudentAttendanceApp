package com.attendance.app.presentation.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.preferences.PreferencesManager
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassesState(
    val classes: List<ClassModel> = emptyList(),
    val selectedClassId: Long = -1L,
    val isAddFormVisible: Boolean = false,
    val newClassName: String = "",
    val newClassSection: String = "",
    val editingClass: ClassModel? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

sealed class ClassesEvent {
    data object ToggleAddForm : ClassesEvent()
    data class UpdateClassName(val name: String) : ClassesEvent()
    data class UpdateClassSection(val section: String) : ClassesEvent()
    data object AddClass : ClassesEvent()
    data class SelectClass(val classModel: ClassModel) : ClassesEvent()
    data class DeleteClass(val classModel: ClassModel) : ClassesEvent()
    data class StartEdit(val classModel: ClassModel) : ClassesEvent()
    data object SaveEdit : ClassesEvent()
    data object CancelEdit : ClassesEvent()
    data object Refresh : ClassesEvent()
}

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ClassesState())
    val state: StateFlow<ClassesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            classRepository.getAllClasses().combine(
                preferencesManager.selectedClassIdFlow
            ) { classes, selectedId ->
                _state.update {
                    it.copy(
                        classes = classes,
                        selectedClassId = selectedId,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: ClassesEvent) {
        when (event) {
            is ClassesEvent.ToggleAddForm -> {
                _state.update {
                    it.copy(
                        isAddFormVisible = !it.isAddFormVisible,
                        newClassName = "",
                        newClassSection = "",
                        editingClass = null
                    )
                }
            }
            is ClassesEvent.UpdateClassName -> _state.update { it.copy(newClassName = event.name) }
            is ClassesEvent.UpdateClassSection -> _state.update { it.copy(newClassSection = event.section) }
            is ClassesEvent.AddClass -> addClass()
            is ClassesEvent.SelectClass -> selectClass(event.classModel)
            is ClassesEvent.DeleteClass -> deleteClass(event.classModel)
            is ClassesEvent.StartEdit -> {
                _state.update {
                    it.copy(
                        editingClass = event.classModel,
                        newClassName = event.classModel.name,
                        newClassSection = event.classModel.section,
                        isAddFormVisible = true
                    )
                }
            }
            is ClassesEvent.SaveEdit -> saveEdit()
            is ClassesEvent.CancelEdit -> {
                _state.update {
                    it.copy(editingClass = null, isAddFormVisible = false, newClassName = "", newClassSection = "")
                }
            }
            is ClassesEvent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            // Since we are observing a Flow from Room, it updates automatically.
            // We just add a small delay to show the animation.
            kotlinx.coroutines.delay(800)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun addClass() {
        val name = _state.value.newClassName.trim()
        val section = _state.value.newClassSection.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = classRepository.insertClass(ClassModel(name = name, section = section))
            preferencesManager.setSelectedClassId(id)
            _state.update { it.copy(isAddFormVisible = false, newClassName = "", newClassSection = "") }
        }
    }

    private fun selectClass(classModel: ClassModel) {
        viewModelScope.launch {
            preferencesManager.setSelectedClassId(classModel.id)
        }
    }

    private fun deleteClass(classModel: ClassModel) {
        viewModelScope.launch {
            classRepository.deleteClass(classModel)
            if (_state.value.selectedClassId == classModel.id) {
                val remaining = _state.value.classes.filter { it.id != classModel.id }
                preferencesManager.setSelectedClassId(remaining.firstOrNull()?.id ?: -1L)
            }
        }
    }

    private fun saveEdit() {
        val editing = _state.value.editingClass ?: return
        val name = _state.value.newClassName.trim()
        val section = _state.value.newClassSection.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            classRepository.updateClass(editing.copy(name = name, section = section))
            _state.update { it.copy(editingClass = null, isAddFormVisible = false, newClassName = "", newClassSection = "") }
        }
    }
}
