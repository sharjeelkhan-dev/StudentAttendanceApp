package com.attendance.app.domain.repository

import kotlinx.coroutines.flow.Flow


data class AiChatMessage(
    val text: String,
    val isUser: Boolean
)

interface AiRepository {

    fun getAttendanceInsights(summaryData: String): Flow<String>

    fun processAiCommand(
        prompt: String,
        history: List<AiChatMessage> = emptyList()
    ): Flow<String>
}
