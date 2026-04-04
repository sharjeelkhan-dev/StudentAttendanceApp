package com.attendance.app.presentation.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.Student
import com.attendance.app.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TakeAttendanceScreen(
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        AttendanceContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun AttendanceContent(
    state: AttendanceState,
    onEvent: (AttendanceEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredStudents = if (state.searchQuery.isBlank()) {
        state.students
    } else {
        state.students.filter {
            it.student.fullName.contains(state.searchQuery, ignoreCase = true) ||
            it.student.rollNumber.contains(state.searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            AttendanceHeader(
                className = state.selectedClass?.name ?: "No Class",
                presentCount = state.presentCount,
                absentCount = state.absentCount,
                isSaving = state.isSaving,
                isSaved = state.isSaved,
                onSave = { onEvent(AttendanceEvent.SaveAttendance) }
            )
        }

        // Search bar
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(AttendanceEvent.SearchQueryChanged(it)) },
                placeholder = { Text("Search student...", color = TextSecondaryLight) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryLight)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = DividerColor,
                    focusedBorderColor = PrimaryGreen
                ),
                singleLine = true
            )
        }

        // All Present / All Absent buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(AttendanceEvent.MarkAllPresent) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PresentGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PresentGreen)
                ) {
                    Text("All Present", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { onEvent(AttendanceEvent.MarkAllAbsent) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AbsentRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AbsentRed)
                ) {
                    Text("All Absent", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Student list
        items(filteredStudents, key = { it.student.id }) { studentState ->
            AttendanceStudentRow(
                studentState = studentState,
                avatarColor = AvatarColors[(studentState.student.id % AvatarColors.size).toInt()],
                onToggle = { status ->
                    onEvent(AttendanceEvent.ToggleStatus(studentState.student.id, status))
                }
            )
        }
    }
}

@Composable
private fun AttendanceHeader(
    className: String,
    presentCount: Int,
    absentCount: Int,
    isSaving: Boolean,
    isSaved: Boolean,
    onSave: () -> Unit
) {
    val today = LocalDate.now()
    val dateFormatted = today.format(
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryGreenDark)
            .padding(top = 16.dp, bottom = 24.dp)
            .padding(horizontal = 20.dp)
    ) {
        Column {
            Text(
                text = "$dateFormatted \u2014 $className",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Take Attendance",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 26.sp,
                            lineHeight = 32.sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$presentCount Present \u00b7 $absentCount Absent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryGreen
                    )
                ) {
                    Text(
                        text = if (isSaved) "Saved" else "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (isSaved) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStudentRow(
    studentState: StudentAttendanceState,
    avatarColor: Color,
    onToggle: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentState.student.initials,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and roll
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentState.student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )
                Text(
                    text = studentState.student.rollNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            // Present button
            val isPresentSelected = studentState.status == AttendanceStatus.PRESENT
            Button(
                onClick = { onToggle(AttendanceStatus.PRESENT) },
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPresentSelected) PresentGreen else PresentGreenBg,
                    contentColor = if (isPresentSelected) Color.White else PresentGreen
                )
            ) {
                Text("P", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Absent button
            val isAbsentSelected = studentState.status == AttendanceStatus.ABSENT
            Button(
                onClick = { onToggle(AttendanceStatus.ABSENT) },
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAbsentSelected) AbsentRed else AbsentRedBg,
                    contentColor = if (isAbsentSelected) Color.White else AbsentRed
                )
            ) {
                Text("A", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttendancePreview() {
    AttendanceTheme {
        AttendanceContent(
            state = AttendanceState(
                selectedClass = ClassModel(name = "Operating Systems", section = "Section A"),
                students = listOf(
                    StudentAttendanceState(
                        student = Student(id = 1, fullName = "John Doe", rollNumber = "CS101", classId = 1),
                        status = AttendanceStatus.PRESENT
                    ),
                    StudentAttendanceState(
                        student = Student(id = 2, fullName = "Jane Smith", rollNumber = "CS102", classId = 1),
                        status = AttendanceStatus.ABSENT
                    )
                ),
                presentCount = 1,
                absentCount = 1,
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
