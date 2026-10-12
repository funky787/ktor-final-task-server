package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import org.example.database.DatabaseFactory
import org.example.dto.ErrorResponse
import org.example.dto.MessageResponse
import org.example.errors.ApiException
import org.example.repository.UserRepository
import org.example.routes.adminRoutes
import org.example.routes.authRoutes
import org.example.routes.taskRoutes
import org.example.routes.webSocketRoutes
import org.example.security.JwtConfig

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module(testing: Boolean = false) {
    DatabaseFactory.init(testing)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    install(CallLogging)
    install(WebSockets)

    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.message))
        }
        exception<ContentTransformationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Неверный JSON"))
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Ошибка сервера"))
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val role = credential.payload.getClaim("role").asString()
                val user = userId?.let(UserRepository::findById)
                if (user != null && user.role.name == role) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Нужен действующий JWT-токен"))
            }
        }
    }

    routing {
        get("/") {
            call.respond(MessageResponse("Ktor server работает"))
        }

        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        authRoutes()
        taskRoutes()
        adminRoutes()
        webSocketRoutes()
    }
}
