package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
    val done: Boolean = false
)

@Serializable
data class UpdateTaskRequest(
    val title: String,
    val description: String,
    val done: Boolean
)
