package com.attendance.app.domain.model

data class ClassModel(
    val id: Long = 0,
    val name: String,
    val section: String,
    val studentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
