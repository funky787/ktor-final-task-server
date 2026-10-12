package org.example.repository

import org.example.database.UsersTable
import org.example.model.Role
import org.example.model.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {
    fun findByLogin(login: String): User? = transaction {
        UsersTable.select { UsersTable.login eq login }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    fun findById(id: Int): User? = transaction {
        UsersTable.select { UsersTable.id eq id }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    fun getAll(): List<User> = transaction {
        UsersTable.selectAll().map(::toUser)
    }

    fun create(login: String, passwordHash: String, role: Role = Role.USER): User = transaction {
        val statement = UsersTable.insert {
            it[UsersTable.login] = login
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role
        }

        User(
            id = statement[UsersTable.id],
            login = login,
            passwordHash = passwordHash,
            role = role
        )
    }

    private fun toUser(row: ResultRow) = User(
        id = row[UsersTable.id],
        login = row[UsersTable.login],
        passwordHash = row[UsersTable.passwordHash],
        role = row[UsersTable.role]
    )
}
