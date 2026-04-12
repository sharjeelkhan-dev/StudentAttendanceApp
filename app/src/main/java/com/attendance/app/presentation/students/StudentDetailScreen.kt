package com.attendance.app.presentation.students
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.presentation.components.StatsCard
import com.attendance.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.StudentDetailScreen(
    studentName: String,
    studentRoll: String,
    initials: String,
    avatarColor: Color,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: StudentDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isDark = LocalIsDarkMode.current
    
    val currentStudentName = state.student?.fullName ?: studentName
    val currentStudentRoll = state.student?.rollNumber ?: studentRoll
    val currentInitials = state.student?.initials ?: initials
    
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Header Section - Maintained as requested (PrimaryGreenDark background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreenDark)
                    .statusBarsPadding()
                    .height(75.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(end = 5.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-5).dp, y = 0.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }

                    Column(modifier = Modifier.padding(top = 14.dp))
                    {
                        Text(
                            text = "Student Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentStudentRoll,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Spacer(modifier = Modifier.height(40.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = CircleShape,
                    color = avatarColor,
                    modifier = Modifier
                        .size(170.dp)
                        .shadow(20.dp, CircleShape, spotColor = avatarColor.copy(alpha = 0.5f))
                        .sharedElement(
                            rememberSharedContentState(key = "avatar_$studentRoll"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentInitials,
                            color = Color.White,
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Verified Badge
                Surface(
                    modifier = Modifier.size(46.dp).offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = PresentGreen,
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name
            Text(
                text = currentStudentName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "name_$studentRoll"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Roll Tag (Light Green Pill)
            Surface(
                color = (if (isDark) Color.Transparent else Color.Transparent),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.offset(y = (-18).dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentStudentRoll,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDark) Color.White else Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-25).dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    label = "Present",
                    value = String.format(Locale.getDefault(), "%02d", state.presentCount),
                    valueColor = PresentGreen,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    label = "Absent",
                    value = String.format(Locale.getDefault(), "%02d", state.absentCount),
                    valueColor = AbsentRed,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    label = "Attendance",
                    value = String.format(Locale.getDefault(), "%.0f%%", state.attendancePercentage),
                    valueColor = Color(0xFF00ACC1),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section Header: Logs (Now outside the card)
            Text(
                text = "Logs",
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 30.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp
            )

            // Attendance Log Card (Refined to match reference image)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-25).dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LogTab(
                            text = "All",
                            isSelected = selectedFilter == "All",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedFilter = "All" }
                        )
                        LogTab(
                            text = "Present",
                            isSelected = selectedFilter == "Present",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedFilter = "Present" }
                        )
                        LogTab(
                            text = "Absent",
                            isSelected = selectedFilter == "Absent",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedFilter = "Absent" }
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    val filteredLogs = remember(state.attendanceLogs, selectedFilter) {
                        when (selectedFilter) {
                            "Present" -> state.attendanceLogs.filter { it.status == AttendanceStatus.PRESENT }
                            "Absent" -> state.attendanceLogs.filter { it.status == AttendanceStatus.ABSENT }
                            else -> state.attendanceLogs
                        }
                    }

                    if (filteredLogs.isEmpty()) {
                        Text(
                            text = "No logs available",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    filteredLogs.forEachIndexed { index, record ->
                        val date = remember(record.date) {
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(record.date)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        val dayStr = date?.let { SimpleDateFormat("EEE", Locale.getDefault()).format(it) } ?: "---"
                        val dayNum = date?.let { SimpleDateFormat("dd", Locale.getDefault()).format(it) } ?: "--"
                        val monthStr = date?.let { SimpleDateFormat("MMM dd", Locale.getDefault()).format(it) } ?: record.date

                        AttendanceLogItem(
                            day = dayStr,
                            date = dayNum,
                            monthDate = monthStr,
                            lectureInfo = "Attendance marked",
                            isPresent = record.status == AttendanceStatus.PRESENT
                        )
                        if (index < filteredLogs.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Info Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val enrollmentDate = remember(state.student?.createdAt) {
                        state.student?.createdAt?.let {
                            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "N/A"
                    }
                    DetailRow(
                        icon = Icons.Rounded.CalendarMonth,
                        label = "Enrollment Date",
                        value = enrollmentDate
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    DetailRow(
                        icon = Icons.Rounded.School,
                        label = "Assigned Class",
                        value = state.classModel?.let { "${it.name} (${it.section})" } ?: "N/A"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LogTab(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) PrimaryGreen else Color.LightGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier,
        border = if (isSelected) null else BorderStroke(1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else if (LocalIsDarkMode.current) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AttendanceLogItem(
    day: String,
    date: String,
    monthDate: String,
    lectureInfo: String,
    isPresent: Boolean
) {
    val statusColor = if (isPresent) PresentGreen else AbsentRed
    val statusBgColor = if (isPresent) PresentGreenBg else AbsentRedBg
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Box
        Column(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(statusColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = date,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.width(18.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = monthDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )
            Text(
                text = lectureInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
        
        // Status Pill
        Surface(
            color = statusBgColor,
            modifier = Modifier.offset(y = (-5).dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (isPresent) "Present" else "Absent",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = PrimaryGreen
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun StudentDetailPreview() {
    AttendanceTheme {
        SharedTransitionLayout {
            AnimatedContent(targetState = true, label = "student_detail_preview") {
                val animatedVisibilityScope = this
                val viewModel = remember { StudentDetailViewModelPreview() }
                Box(modifier = Modifier.fillMaxSize()) {
                    StudentDetailScreen(
                        studentName = "John Doe",
                        studentRoll = "CS-01",
                        initials = "JD",
                        avatarColor = PrimaryGreen,
                        animatedVisibilityScope = animatedVisibilityScope,
                        viewModel = viewModel,
                        onBack = {}
                    )
                }
            }
        }
    }
}

@SuppressLint("VisibleForTests")
class StudentDetailViewModelPreview : StudentDetailViewModel(
    studentRepository = object : com.attendance.app.domain.repository.StudentRepository {
        override fun getStudentsByClass(classId: Long) = error("Not implemented")
        override suspend fun getStudentById(studentId: Long) = com.attendance.app.domain.model.Student(1L, "John Doe", "CS-01", 1L)
        override suspend fun insertStudent(student: com.attendance.app.domain.model.Student) = 0L
        override suspend fun updateStudent(student: com.attendance.app.domain.model.Student) {}
        override suspend fun deleteStudent(student: com.attendance.app.domain.model.Student) {}
        override suspend fun getAttendancePercentage(studentId: Long, classId: Long) = 90.0
        override fun searchStudents(classId: Long, query: String) = error("Not implemented")
    },
    classRepository = object : com.attendance.app.domain.repository.ClassRepository {
        override fun getAllClasses() = error("Not implemented")
        override suspend fun getClassById(classId: Long) = com.attendance.app.domain.model.ClassModel(1L, "Software Engineering", "6C1")
        override suspend fun insertClass(classModel: com.attendance.app.domain.model.ClassModel) = 0L
        override suspend fun updateClass(classModel: com.attendance.app.domain.model.ClassModel) {}
        override suspend fun deleteClass(classModel: com.attendance.app.domain.model.ClassModel) {}
    },
    attendanceRepository = object : com.attendance.app.domain.repository.AttendanceRepository {
        override fun getAttendanceByClassAndDate(classId: Long, date: String) = error("Not implemented")
        override fun getAttendanceByStudent(studentId: Long, classId: Long) = kotlinx.coroutines.flow.flowOf(
            listOf(
                com.attendance.app.domain.model.AttendanceRecord(1, 1, 1, "2024-04-03", AttendanceStatus.PRESENT),
                com.attendance.app.domain.model.AttendanceRecord(2, 1, 1, "2024-04-02", AttendanceStatus.PRESENT),
                com.attendance.app.domain.model.AttendanceRecord(3, 1, 1, "2024-04-01", AttendanceStatus.ABSENT)
            )
        )
        override suspend fun saveAttendance(records: List<com.attendance.app.domain.model.AttendanceRecord>) {}
        override fun getSessionSummary(classId: Long, date: String) = error("Not implemented")
        override fun getRecentSessions(classId: Long, limit: Int) = error("Not implemented")
        override fun getSessionDates(classId: Long) = error("Not implemented")
        override suspend fun getAllAttendanceForClass(classId: Long) = emptyList<com.attendance.app.domain.model.AttendanceRecord>()
        override suspend fun getAllAttendance() = emptyList<com.attendance.app.domain.model.AttendanceRecord>()
    },
    savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("studentId" to 1L, "classId" to 1L))
)

@Preview(showBackground = true)
@Composable
fun AttendanceLogItemPreview() {
    AttendanceTheme {
        AttendanceLogItem(
            day = "Thu",
            date = "03",
            monthDate = "Apr 03",
            lectureInfo = "Lecture 20 . Data Structures",
            isPresent = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InfoSectionPreview() {
    AttendanceTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp)
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                DetailRow(
                    icon = Icons.Rounded.CalendarMonth,
                    label = "Enrollment Date",
                    value = "April 03, 2024"
                )
                Spacer(modifier = Modifier.height(20.dp))
                DetailRow(
                    icon = Icons.Rounded.School,
                    label = "Assigned Class",
                    value = "Software Engineering (6C1)"
                )
            }
        }
    }
}
