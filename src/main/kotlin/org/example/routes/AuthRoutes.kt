package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.example.dto.LoginRequest
import org.example.dto.RegisterRequest
import org.example.dto.TokenResponse
import org.example.dto.UserResponse
import org.example.errors.BadRequestApiException
import org.example.errors.ConflictApiException
import org.example.errors.UnauthorizedApiException
import org.example.repository.UserRepository
import org.example.security.JwtConfig
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.login.isBlank() || request.password.isBlank()) {
                throw BadRequestApiException("Логин и пароль не должны быть пустыми")
            }
            if (request.password.length < 4) {
                throw BadRequestApiException("Пароль должен содержать минимум 4 символа")
            }
            if (UserRepository.findByLogin(request.login) != null) {
                throw ConflictApiException("Пользователь с таким логином уже существует")
            }

            val user = UserRepository.create(
                login = request.login,
                passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
            )

            call.respond(HttpStatusCode.Created, UserResponse(user.id, user.login, user.role))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = UserRepository.findByLogin(request.login)

            if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
                throw UnauthorizedApiException("Неверный логин или пароль")
            }

            call.respond(
                HttpStatusCode.OK,
                TokenResponse(
                    token = JwtConfig.generateToken(user),
                    expiresInSeconds = JwtConfig.expirationTime / 1000,
                    userId = user.id,
                    role = user.role
                )
            )
        }
    }
}
