package org.example.security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import org.example.errors.ForbiddenApiException
import org.example.errors.UnauthorizedApiException
import org.example.model.Role
import org.example.model.User
import org.example.repository.UserRepository

fun ApplicationCall.currentUser(): User {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedApiException("Нужен действующий JWT-токен")
    val userId = principal.payload.getClaim("userId").asInt()
        ?: throw UnauthorizedApiException("Неверный JWT-токен")
    return UserRepository.findById(userId)
        ?: throw UnauthorizedApiException("Пользователь не найден")
}

fun User.requireRole(role: Role) {
    if (this.role != role) {
        throw ForbiddenApiException("Недостаточно прав")
    }
}
