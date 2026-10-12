package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val done: Boolean,
    val ownerId: Int
)
