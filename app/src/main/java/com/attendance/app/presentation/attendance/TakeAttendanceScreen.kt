package com.attendance.app.presentation.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attendance.app.R
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.Student
import com.attendance.app.presentation.components.StandardHeader
import com.attendance.app.presentation.components.VerticalScrollbar
import com.attendance.app.presentation.theme.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TakeAttendanceScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            showLoading = true
        } else {
            delay(800.milliseconds)
            showLoading = false
        }
    }

    AttendanceContent(
        state = state,
        showLoading = showLoading,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        paddingValues = paddingValues
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceContent(
    state: AttendanceState,
    showLoading: Boolean,
    onEvent: (AttendanceEvent) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    // 💡 Performance Fix: Filtering deferred to derivedStateOf (Prevents recalculation on scroll)
    val filteredStudents by remember(state.students, state.searchQuery) {
        derivedStateOf {
            if (state.searchQuery.isBlank()) {
                state.students
            } else {
                state.students.filter {
                    it.student.fullName.contains(state.searchQuery, ignoreCase = true) ||
                            it.student.rollNumber.contains(state.searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val isDark = LocalIsDarkMode.current
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
            .background(MaterialTheme.colorScheme.background)
    ) {
        StandardHeader(
            title = "Take Attendance",
            subtitle = "${state.selectedClass?.name ?: "No Class"} · ${state.presentCount} Present · ${state.absentCount} Absent",
            showDate = true,
            date = state.attendanceDate,
            showSave = true,
            onSaveClick = { onEvent(AttendanceEvent.SaveAttendance) },
            isSaving = state.isSaving,
            isSaved = state.isSaved
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(AttendanceEvent.Refresh) },
            state = pullToRefreshState,
            modifier = Modifier.weight(1f),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = if (isDark) MaterialTheme.colorScheme.primary else PrimaryGreen
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    // Search Bar (Original Layout)
                    item(key = "search_bar") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFF2F4F7),
                                tonalElevation = 2.dp,
                                border = if (isDark)
                                    androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                else
                                    androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                TextField(
                                    value = state.searchQuery,
                                    onValueChange = { onEvent(AttendanceEvent.SearchQueryChanged(it)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Search student...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.magnifying_glass_icon),
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(20.dp).offset(x = 5.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (state.searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { onEvent(AttendanceEvent.SearchQueryChanged("")) }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = if (isDark) MaterialTheme.colorScheme.primary else PrimaryGreen
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Student List Section
                    if (filteredStudents.isEmpty() && !state.isLoading) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.searchQuery.isBlank())
                                        "No students found in this class.\nAdd students to start taking attendance!"
                                    else
                                        "No students match your search.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    } else {
                        items(
                            items = filteredStudents,
                            key = { it.student.id }
                        ) { studentState ->
                            val avatarColor = remember(studentState.student.fullName) {
                                getAvatarColor(studentState.student.fullName)
                            }

                            AttendanceStudentRow(
                                studentState = studentState,
                                avatarColor = avatarColor,
                                onToggle = { status ->
                                    onEvent(AttendanceEvent.ToggleStatus(studentState.student.id, status))
                                }
                            )
                        }
                    }
                }

                VerticalScrollbar(
                    lazyListState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = if (isDark) MaterialTheme.colorScheme.primary else PrimaryGreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceStudentRow(
    studentState: StudentAttendanceState,
    avatarColor: Color,
    onToggle: (AttendanceStatus) -> Unit
) {
    val isDark = LocalIsDarkMode.current

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = Color.Gray.copy(alpha = 0.15f)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            onClick = { /* Could navigate or show details */ }
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
                        color = AvatarTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Roll Number
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = studentState.student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = studentState.student.rollNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Present Button (Aesthetics Exact Same, Internal Mechanics Optimized)
                val isPresentSelected = studentState.status == AttendanceStatus.PRESENT
                AttendanceActionButton(
                    text = "P",
                    isSelected = isPresentSelected,
                    selectedBg = PresentGreen,
                    unselectedBg = if (isDark) DividerColorDark else PresentGreen.copy(alpha = 0.1f),
                    selectedFg = Color.White,
                    unselectedFg = PresentGreen,
                    onClick = { onToggle(AttendanceStatus.PRESENT) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Absent Button (Aesthetics Exact Same, Internal Mechanics Optimized)
                val isAbsentSelected = studentState.status == AttendanceStatus.ABSENT
                AttendanceActionButton(
                    text = "A",
                    isSelected = isAbsentSelected,
                    selectedBg = AbsentRed,
                    unselectedBg = if (isDark) DividerColorDark else AbsentRed.copy(alpha = 0.1f),
                    selectedFg = Color.White,
                    unselectedFg = AbsentRed,
                    onClick = { onToggle(AttendanceStatus.ABSENT) }
                )
            }
        }
    }
}

// ⚡ Custom Action Button keeping exact same 40dp Circle, Elevation & Colors without M3 Button Lag
@Composable
private fun AttendanceActionButton(
    text: String,
    isSelected: Boolean,
    selectedBg: Color,
    unselectedBg: Color,
    selectedFg: Color,
    unselectedFg: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ),
        shape = RoundedCornerShape(28.dp),
        color = if (isSelected) selectedBg else unselectedBg,
        contentColor = if (isSelected) selectedFg else unselectedFg,
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun TakeAttendancePreviewLight() {
    AttendanceTheme(darkTheme = false) {
        AttendanceContent(
            state = AttendanceState(
                selectedClass = ClassModel(id = 1, name = "Software Engineering", section = "6C1"),
                students = listOf(
                    StudentAttendanceState(
                        student = Student(id = 1, fullName = "John Doe", rollNumber = "2021-SE-01", classId = 1),
                        status = AttendanceStatus.PRESENT
                    ),
                    StudentAttendanceState(
                        student = Student(id = 2, fullName = "Jane Smith", rollNumber = "2021-SE-02", classId = 1),
                        status = AttendanceStatus.ABSENT
                    )
                ),
                presentCount = 1,
                absentCount = 1,
                isLoading = false
            ),
            showLoading = false,
            onEvent = {}
        )
    }
}