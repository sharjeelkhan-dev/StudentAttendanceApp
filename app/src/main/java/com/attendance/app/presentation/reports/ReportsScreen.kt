package com.attendance.app.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.domain.model.ClassModel
import com.attendance.app.domain.model.SessionSummary
import com.attendance.app.domain.model.Student
import com.attendance.app.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        ReportsContent(
            state = state,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        )
    }
}

@Composable
private fun ReportsContent(
    state: ReportsState,
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
                    .padding(horizontal = 20.dp)
            ) {
                Column {
                    Text(
                        text = buildString {
                            state.selectedClass?.let {
                                append("${it.name} \u2014 ${it.section}")
                            } ?: append("No Class Selected")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Attendance Report",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }
            }
        }

        // Student Overview section
        item {
            Text(
                text = "STUDENT OVERVIEW",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            )
        }

        if (state.studentReports.isEmpty() && !state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No students or attendance data yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(state.studentReports, key = { it.student.id }) { report ->
                ReportStudentRow(
                    initials = report.student.initials,
                    name = report.student.fullName,
                    percentage = report.attendancePercentage,
                    avatarColor = AvatarColors[(report.student.id % AvatarColors.size).toInt()]
                )
            }
        }

        // Session Details section
        item {
            Text(
                text = "SESSION DETAILS",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
            )
        }

        if (state.sessionDetails.isEmpty() && !state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No session data available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(state.sessionDetails) { session ->
                NewSessionCard(
                    date = session.summary.date,
                    presentCount = session.summary.presentCount,
                    totalCount = session.summary.totalStudents,
                    sessionDetails = session
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewSessionCard(
    date: String,
    presentCount: Int,
    totalCount: Int,
    sessionDetails: SessionWithRecords
) {
    val parsedDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }
    val displayDate = parsedDate.format(DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "$presentCount/$totalCount present",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sessionDetails.students.forEach { student ->
                    val record = sessionDetails.records.find { it.studentId == student.id }
                    val isPresent = record?.status == AttendanceStatus.PRESENT
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPresent) PresentGreen.copy(alpha = 0.12f) else AbsentRed.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.initials,
                            color = if (isPresent) PresentGreen else AbsentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStudentRow(
    initials: String,
    name: String,
    percentage: Double,
    avatarColor: Color
) {
    val percentageColor = when {
        percentage >= 80 -> PresentGreen
        percentage >= 50 -> LateOrange
        else -> AbsentRed
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .offset(y = 12.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                color = TextPrimaryLight
            )

            Text(
                text = "${percentage.toInt()}%",
                color = percentageColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        LinearProgressIndicator(
            progress = { (percentage / 100f).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = percentageColor,
            trackColor = Color(0xFFEEEEEE),
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    val dummyStudents = listOf(
        Student(id = 1, fullName = "John Smith", rollNumber = "101", classId = 1),
        Student(id = 2, fullName = "Emily Davis", rollNumber = "102", classId = 1),
        Student(id = 3, fullName = "Michael Brown", rollNumber = "103", classId = 1)
    )

    val dummyReports = listOf(
        StudentReport(dummyStudents[0], 92.0),
        StudentReport(dummyStudents[1], 65.0),
        StudentReport(dummyStudents[2], 88.0)
    )

    val dummyRecords = listOf(
        AttendanceRecord(studentId = 1, classId = 1, date = "2024-05-10", status = AttendanceStatus.PRESENT),
        AttendanceRecord(studentId = 2, classId = 1, date = "2024-05-10", status = AttendanceStatus.ABSENT),
        AttendanceRecord(studentId = 3, classId = 1, date = "2024-05-10", status = AttendanceStatus.PRESENT)
    )

    val dummySession = SessionWithRecords(
        summary = SessionSummary("2024-05-10", 3, 2, 1),
        records = dummyRecords,
        students = dummyStudents
    )

    AttendanceTheme {
        ReportsContent(
            state = ReportsState(
                selectedClass = ClassModel(1, "Computer Science", "Section A"),
                studentReports = dummyReports,
                sessionDetails = listOf(dummySession),
                isLoading = false
            )
        )
    }
}
