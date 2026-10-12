package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.example.dto.UserResponse
import org.example.model.Role
import org.example.repository.UserRepository
import org.example.security.currentUser
import org.example.security.requireRole

fun Route.adminRoutes() {
    authenticate("auth-jwt") {
        route("/admin") {
            get("/users") {
                call.currentUser().requireRole(Role.ADMIN)
                val users = UserRepository.getAll().map { UserResponse(it.id, it.login, it.role) }
                call.respond(HttpStatusCode.OK, users)
            }
        }
    }
}
