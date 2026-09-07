package com.attendance.app.data.repository

import android.util.Log
import com.attendance.app.data.preferences.PreferencesManager
import com.attendance.app.domain.model.AttendanceRecord
import com.attendance.app.domain.model.AttendanceStatus
import com.attendance.app.domain.repository.AiChatMessage
import com.attendance.app.domain.repository.AiRepository
import com.attendance.app.domain.repository.AttendanceRepository
import com.attendance.app.domain.repository.ClassRepository
import com.attendance.app.domain.repository.StudentRepository
import com.google.ai.client.generativeai.GenerativeModel as GoogleGenerativeModel
import com.google.ai.client.generativeai.type.content as googleContent
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content as firebaseContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.takeLast
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class FirebaseAiRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : AiRepository {

    // Helper to build history for Gemini SDK
    private fun List<AiChatMessage>.toFirebaseHistory(): List<Content> {
        return map { msg ->
            firebaseContent(role = if (msg.isUser) "user" else "model") {
                text(msg.text)
            }
        }
    }

    private fun List<AiChatMessage>.toGoogleHistory(): List<com.google.ai.client.generativeai.type.Content> {
        return map { msg ->
            googleContent(role = if (msg.isUser) "user" else "model") {
                text(msg.text)
            }
        }
    }

    // Helper function with exponential backoff retry mechanism for AI requests
    private suspend fun generateContentWithRetry(prompt: String, maxRetries: Int = 3): String? = withContext(Dispatchers.IO) {
        val userApiKey = preferencesManager.aiApiKeyFlow.first()?.trim()
        var currentDelay = 1500L // 1.5 Seconds initial delay to clear brief quota window

        for (attempt in 0 until maxRetries) {
            try {
                if (userApiKey.isNullOrBlank()) {
                    Log.d("FirebaseAiRepo", "Using default Firebase-managed Gemini model (Attempt ${attempt + 1})")
                    val firebaseModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                        .generativeModel("gemini-3.5-flash")

                    return@withContext firebaseModel.generateContent(prompt).text
                } else {
                    Log.d("FirebaseAiRepo", "Using custom API key with Google AI SDK (Attempt ${attempt + 1})")
                    val googleModel = GoogleGenerativeModel(
                        modelName = "gemini-3.5-flash",
                        apiKey = userApiKey
                    )
                    return@withContext googleModel.generateContent(prompt).text
                }
            } catch (e: Exception) {
                Log.e("FirebaseAiRepo", "Error on attempt ${attempt + 1}", e)
                val errorMsg = e.message?.lowercase() ?: ""
                val isQuotaError = errorMsg.contains("429") || errorMsg.contains("quota") || errorMsg.contains("exhausted")

                if (isQuotaError && attempt < maxRetries - 1) {
                    Log.w("FirebaseAiRepo", "Quota hit (RESOURCE_EXHAUSTED). Retrying in $currentDelay ms...")
                    delay(currentDelay.milliseconds)
                    currentDelay *= 2 // Exponential backoff (1.5s -> 3s -> 6s)
                } else {
                    // Critical error or last attempt exhausted, map standard exceptions safely
                    val errorMessage = when {
                        errorMsg.contains("403") -> "Access Forbidden (403). Check App Check or API restrictions."
                        errorMsg.contains("api_key_invalid") -> "Invalid API Key. Please check your key in Settings."
                        isQuotaError -> "Rate limit reached (RESOURCE_EXHAUSTED). Please wait a few moments."
                        else -> "AI Engine Error: ${e.message}"
                    }
                    throw Exception(errorMessage)
                }
            }
        }
        null
    }

    override fun getAttendanceInsights(summaryData: String): Flow<String> = flow {
        try {
            Log.d("FirebaseAiRepo", "Requesting attendance insights")
            // Input data optimization to keep token payload strict and clean
            val optimizedData = if (summaryData.length > 1500) summaryData.take(1500) else summaryData

            val prompt = """
                You are an expert educational consultant. Analyze the following student attendance data and provide 3 short, actionable insights or recommendations for the teacher to improve attendance or identify risks.
                
                Data:
                $optimizedData
                
                Requirements:
                1. Use a professional and encouraging tone.
                2. Keep each insight concise (under 20 words).
                3. Focus on patterns or specific concerns.
                4. Output only the 3 bullet points.
            """.trimIndent()

            val responseText = generateContentWithRetry(prompt) ?: throw Exception("Empty AI response")
            emit(responseText)
        } catch (e: Exception) {
            Log.e("FirebaseAiRepo", "Error during insights generation", e)
            emit("Could not generate insights: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override fun processAiCommand(
        prompt: String,
        history: List<AiChatMessage>
    ): Flow<String> = flow {
        try {
            Log.d("FirebaseAiRepo", "Processing AI command with history: $prompt")

            // 1. Fetch Context (Classes and Students)
            val allClasses = classRepository.getAllClasses().first()
            val classesContext = allClasses.joinToString("\n") { "Class: ${it.name}, Section: ${it.section}" }

            val selectedClassId = preferencesManager.selectedClassIdFlow.first()
            val selectedClass = allClasses.find { it.id == selectedClassId }

            val studentsContext = if (selectedClass != null) {
                val students = studentRepository.getStudentsByClass(selectedClass.id).first()
                "Current Selected Class: ${selectedClass.name} (Section: ${selectedClass.section})\n" +
                "Students in this class:\n" + students.joinToString("\n") { "- ${it.fullName} (Roll: ${it.rollNumber})" }
            } else ""

            val systemPrompt = """
                You are a smart AI Assistant for a Student Attendance App. 
                Today is ${LocalDate.now()}.
                
                App Context:
                Available Classes:
                $classesContext
                
                Current Active Class: ${selectedClass?.name ?: "None"}
                $studentsContext
                
                Capabilities & Actions:
                1. Navigation:
                   - If the user wants to go to a screen, use: ACTION:NAVIGATE|ROUTE:route_name
                   - Routes: home, take_attendance, reports, students, settings, classes.
                2. Take/Save Attendance (SINGLE COMMAND):
                   - If the user says "Save attendance", "Mark attendance", or "Take attendance":
                     - Use the "Current Active Class": ${selectedClass?.name ?: "None"}.
                     - Assume ALL students are present unless specifically told otherwise.
                     - Use TODAY'S DATE: ${LocalDate.now()}.
                     - Format: ACTION:MARK_ATTENDANCE|CLASS:Name|DATE:YYYY-MM-DD|PRESENT:ALL|ABSENT:
                   - If specific students are mentioned (e.g., "Roll 5 is absent"), include them in ABSENT.
                   - Output this block to trigger an automatic save to the database.
                3. General Control:
                   - Always be direct. If you have enough context to perform an action, DO IT immediately.
                   
                Strict Logic:
                - If you output an ACTION:MARK_ATTENDANCE line, the app will SAVE it to the database instantly.
                - Tell the user: "I have saved the attendance for [Class Name]."
                - ALWAYS output the ACTION line at the end of your response if an action was requested.
                
                User Query: $prompt
            """.trimIndent()

            // 2. Build full prompt with history
            val fullPrompt = history.takeLast(10).joinToString("\n") {
                if (it.isUser) "User: ${it.text}" else "Assistant: ${it.text}"
            } + "\nUser: $systemPrompt"

            val responseText = generateContentWithRetry(fullPrompt) ?: throw Exception("Empty AI response")

            // 3. Handle Actions
            if (responseText.contains("ACTION:MARK_ATTENDANCE")) {
                val actionBlock = responseText.lines().find { it.contains("ACTION:MARK_ATTENDANCE") }
                actionBlock?.let { 
                    Log.d("FirebaseAiRepo", "Executing AI Save Action: $it")
                    handleMarkAttendanceAction(it) 
                }
            }

            emit(responseText)
        } catch (e: Exception) {
            Log.e("FirebaseAiRepo", "Error during command processing", e)
            emit("Error processing command: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private suspend fun handleMarkAttendanceAction(actionStr: String) {
        try {
            // Format: ACTION:MARK_ATTENDANCE|CLASS:Name|DATE:YYYY-MM-DD|PRESENT:Roll1,Roll2|ABSENT:Roll3,Roll4
            val parts = actionStr.split("|")
            val className = parts.find { it.startsWith("CLASS:") }?.removePrefix("CLASS:")?.trim() ?: ""
            val date = parts.find { it.startsWith("DATE:") }?.removePrefix("DATE:")?.trim() ?: LocalDate.now().toString()

            val presentRolls = parts.find { it.startsWith("PRESENT:") }?.removePrefix("PRESENT:")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            val absentRolls = parts.find { it.startsWith("ABSENT:") }?.removePrefix("ABSENT:")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

            val allPresent = presentRolls.any { it.equals("ALL", ignoreCase = true) }
            val allAbsent = absentRolls.any { it.equals("ALL", ignoreCase = true) }

            // Backwards compatibility for old format if needed
            val legacyRolls = parts.find { it.startsWith("STUDENTS:") }?.removePrefix("STUDENTS:")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            val finalPresentRolls = presentRolls.ifEmpty { legacyRolls }

            val classes = classRepository.getAllClasses().first()
            val targetClass = if (className.isNotEmpty()) {
                classes.find { it.name.equals(className, ignoreCase = true) } ?: classes.firstOrNull()
            } else {
                val selectedClassId = preferencesManager.selectedClassIdFlow.first()
                classes.find { it.id == selectedClassId } ?: classes.firstOrNull()
            }

            if (targetClass != null) {
                val students = studentRepository.getStudentsByClass(targetClass.id).first()
                val records = students.map { student ->
                    val isPresent = allPresent || finalPresentRolls.any { it.equals(student.rollNumber, ignoreCase = true) || it.equals(student.fullName, ignoreCase = true) }
                    val isExplicitlyAbsent = allAbsent || absentRolls.any { it.equals(student.rollNumber, ignoreCase = true) || it.equals(student.fullName, ignoreCase = true) }

                    AttendanceRecord(
                        studentId = student.id,
                        classId = targetClass.id,
                        date = date,
                        status = when {
                            isExplicitlyAbsent -> AttendanceStatus.ABSENT
                            isPresent -> AttendanceStatus.PRESENT
                            else -> AttendanceStatus.ABSENT 
                        }
                    )
                }
                attendanceRepository.saveAttendance(records)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAiRepo", "Failed to execute AI action", e)
        }
    }
}