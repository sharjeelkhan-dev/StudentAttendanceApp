package com.attendance.app.presentation.classes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.presentation.theme.*

@Composable
fun ClassesScreen(
    onBack: () -> Unit,
    viewModel: ClassesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        ClassesContent(
            state = state,
            onBack = onBack,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        )
    }
}

@Composable
private fun ClassesContent(
    state: ClassesState,
    onBack: () -> Unit,
    onEvent: (ClassesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreenDark)
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 24.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Manage Classes",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier= Modifier.offset(x = 55.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                }
            }
        }

        // Add form
        item {
            AnimatedVisibility(
                visible = state.isAddFormVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (state.editingClass != null) "Edit Class" else "Add New Class",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.newClassName,
                            onValueChange = { onEvent(ClassesEvent.UpdateClassName(it)) },
                            placeholder = { Text("Class Name (e.g. Data Structures)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = DividerColor,
                                focusedBorderColor = PrimaryGreen
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.newClassSection,
                            onValueChange = { onEvent(ClassesEvent.UpdateClassSection(it)) },
                            placeholder = { Text("Section (e.g. Section A)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = DividerColor,
                                focusedBorderColor = PrimaryGreen
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (state.editingClass != null) onEvent(ClassesEvent.SaveEdit)
                                    else onEvent(ClassesEvent.AddClass)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    if (state.editingClass != null) "Save" else "Add Class",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TextButton(onClick = {
                                if (state.editingClass != null) onEvent(ClassesEvent.CancelEdit)
                                else onEvent(ClassesEvent.ToggleAddForm)
                            }) {
                                Text("Cancel", color = TextSecondaryLight)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !state.isAddFormVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedButton(
                    onClick = { onEvent(ClassesEvent.ToggleAddForm) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreenDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreenDark)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null,
                        modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Class",
                        modifier = Modifier.offset(x = (-5).dp),
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Classes Header
        item {
            Text(
                text = "YOUR CLASSES",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        if (state.classes.isEmpty() && !state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes yet.\nCreate your first class to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(state.classes, key = { it.id }) { classModel ->
                val isSelected = classModel.id == state.selectedClassId
                ClassRow(
                    classModel = classModel,
                    isSelected = isSelected,
                    onSelect = { onEvent(ClassesEvent.SelectClass(classModel)) },
                    onEdit = { onEvent(ClassesEvent.StartEdit(classModel)) },
                    onDelete = { onEvent(ClassesEvent.DeleteClass(classModel)) }
                )
            }
        }
    }
}

@Composable
private fun ClassRow(
    classModel: ClassModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .then(
                if (isSelected) Modifier.border(2.dp, PrimaryGreen, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGreenSurface else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classModel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )
                if (classModel.section.isNotBlank()) {
                    Text(
                        text = classModel.section,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondaryLight, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AbsentRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Classes List")
@Composable
fun ClassesPreview() {
    AttendanceTheme {
        ClassesContent(
            state = ClassesState(
                classes = listOf(
                    ClassModel(id = 1, name = "Computer Science", section = "Section A"),
                    ClassModel(id = 2, name = "Mathematics", section = "Section B")
                ),
                selectedClassId = 1,
                isLoading = false
            ),
            onBack = {},
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Add Class Form")
@Composable
fun AddClassPreview() {
    AttendanceTheme {
        ClassesContent(
            state = ClassesState(
                classes = emptyList(),
                isAddFormVisible = true,
                newClassName = "Mobile Development",
                newClassSection = "6th Semester",
                isLoading = false
            ),
            onBack = {},
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty Classes")
@Composable
fun EmptyClassesPreview() {
    AttendanceTheme {
        ClassesContent(
            state = ClassesState(
                classes = emptyList(),
                isLoading = false
            ),
            onBack = {},
            onEvent = {}
        )
    }
}
