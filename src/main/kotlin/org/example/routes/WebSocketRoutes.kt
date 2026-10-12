package org.example.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.dto.TaskEvent
import org.example.websocket.TaskEventHub

fun Route.webSocketRoutes() {
    webSocket("/ws/tasks") {
        TaskEventHub.add(this)
        try {
            send(Frame.Text(Json.encodeToString(TaskEvent("CONNECTED"))))
            for (frame in incoming) {
                if (frame is Frame.Text && frame.readText().equals("ping", ignoreCase = true)) {
                    send(Frame.Text(Json.encodeToString(TaskEvent("PONG"))))
                }
            }
        } finally {
            TaskEventHub.remove(this)
        }
    }
}
