package org.example.database

import org.example.model.Role
import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 64).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val role = enumerationByName("role", 16, Role::class)

    override val primaryKey = PrimaryKey(id)
}

object TasksTable : Table("tasks") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = varchar("description", 1000)
    val done = bool("done")
    val ownerId = integer("owner_id").references(UsersTable.id)

    override val primaryKey = PrimaryKey(id)
}
