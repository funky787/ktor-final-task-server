package org.example.websocket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.dto.TaskEvent
import java.util.concurrent.ConcurrentHashMap

object TaskEventHub {
    private val sessions = ConcurrentHashMap.newKeySet<DefaultWebSocketServerSession>()
    private val json = Json { encodeDefaults = true }

    fun add(session: DefaultWebSocketServerSession) {
        sessions.add(session)
    }

    fun remove(session: DefaultWebSocketServerSession) {
        sessions.remove(session)
    }

    suspend fun broadcast(event: TaskEvent) {
        val text = json.encodeToString(event)
        val closed = mutableListOf<DefaultWebSocketServerSession>()

        sessions.forEach { session ->
            try {
                session.send(Frame.Text(text))
            } catch (_: Exception) {
                closed.add(session)
            }
        }

        closed.forEach(sessions::remove)
    }
}
