package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.example.dto.CreateTaskRequest
import org.example.dto.TaskEvent
import org.example.dto.UpdateTaskRequest
import org.example.errors.BadRequestApiException
import org.example.errors.ForbiddenApiException
import org.example.errors.NotFoundApiException
import org.example.model.Role
import org.example.repository.TaskRepository
import org.example.security.currentUser
import org.example.websocket.TaskEventHub

fun Route.taskRoutes() {
    route("/tasks") {
        get {
            call.respond(HttpStatusCode.OK, TaskRepository.getAll())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestApiException("Неверный id")
            val task = TaskRepository.getById(id)
                ?: throw NotFoundApiException("Задача не найдена")
            call.respond(HttpStatusCode.OK, task)
        }

        authenticate("auth-jwt") {
            post {
                val user = call.currentUser()
                val request = call.receive<CreateTaskRequest>()
                if (request.title.isBlank()) {
                    throw BadRequestApiException("Название задачи не должно быть пустым")
                }

                val task = TaskRepository.create(request, user.id)
                TaskEventHub.broadcast(TaskEvent("TASK_CREATED", task.id, task.title, user.id))
                call.respond(HttpStatusCode.Created, task)
            }

            put("/{id}") {
                val user = call.currentUser()
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestApiException("Неверный id")
                val existing = TaskRepository.getById(id)
                    ?: throw NotFoundApiException("Задача не найдена")

                if (user.role != Role.ADMIN && existing.ownerId != user.id) {
                    throw ForbiddenApiException("Можно изменять только свои задачи")
                }

                val request = call.receive<UpdateTaskRequest>()
                if (request.title.isBlank()) {
                    throw BadRequestApiException("Название задачи не должно быть пустым")
                }

                val task = TaskRepository.update(id, request)
                    ?: throw NotFoundApiException("Задача не найдена")
                TaskEventHub.broadcast(TaskEvent("TASK_UPDATED", task.id, task.title, user.id))
                call.respond(HttpStatusCode.OK, task)
            }

            delete("/{id}") {
                val user = call.currentUser()
                if (user.role != Role.ADMIN) {
                    throw ForbiddenApiException("Удалять задачи может только ADMIN")
                }

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestApiException("Неверный id")
                if (!TaskRepository.delete(id)) {
                    throw NotFoundApiException("Задача не найдена")
                }

                TaskEventHub.broadcast(TaskEvent("TASK_DELETED", id, userId = user.id))
                call.response.status(HttpStatusCode.NoContent)
            }
        }
    }
}
