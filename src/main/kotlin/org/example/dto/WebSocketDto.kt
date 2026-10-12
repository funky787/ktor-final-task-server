package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskEvent(
    val type: String,
    val taskId: Int? = null,
    val title: String? = null,
    val userId: Int? = null
)
