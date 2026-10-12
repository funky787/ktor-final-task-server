package org.example.model

data class User(
    val id: Int,
    val login: String,
    val passwordHash: String,
    val role: Role
)
