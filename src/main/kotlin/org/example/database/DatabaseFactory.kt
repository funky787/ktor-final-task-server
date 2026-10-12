package org.example.database

import org.example.model.Role
import org.example.repository.UserRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

object DatabaseFactory {
    fun init(testing: Boolean = false) {
        val url = if (testing) {
            "jdbc:h2:mem:test-${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
        } else {
            System.getenv("DB_URL") ?: "jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
        }

        val driver = if (url.startsWith("jdbc:postgresql:")) {
            "org.postgresql.Driver"
        } else {
            "org.h2.Driver"
        }

        Database.connect(
            url = url,
            driver = driver,
            user = System.getenv("DB_USER") ?: "sa",
            password = System.getenv("DB_PASSWORD") ?: ""
        )

        transaction {
            SchemaUtils.create(UsersTable, TasksTable)
        }

        seedAdmin()
    }

    private fun seedAdmin() {
        val login = System.getenv("ADMIN_LOGIN") ?: "admin"
        val password = System.getenv("ADMIN_PASSWORD") ?: "admin123"

        if (UserRepository.findByLogin(login) == null) {
            UserRepository.create(
                login = login,
                passwordHash = BCrypt.hashpw(password, BCrypt.gensalt()),
                role = Role.ADMIN
            )
        }
    }
}
