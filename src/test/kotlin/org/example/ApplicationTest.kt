package org.example

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `public tasks route works`() = testApplication {
        application { module(testing = true) }
        val response = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `swagger is available`() = testApplication {
        application { module(testing = true) }
        val response = client.get("/swagger/index.html")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `protected route returns 401 without token`() = testApplication {
        application { module(testing = true) }
        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Task"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `registration and login return jwt`() = testApplication {
        application { module(testing = true) }
        val login = "user1"
        val register = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"$login","password":"123456"}""")
        }
        assertEquals(HttpStatusCode.Created, register.status)

        val token = login(login, "123456")
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `user cannot access admin route`() = testApplication {
        application { module(testing = true) }
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"normal","password":"123456"}""")
        }
        val token = login("normal", "123456")
        val response = client.get("/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `admin can access admin route`() = testApplication {
        application { module(testing = true) }
        val token = login("admin", "admin123")
        val response = client.get("/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "ADMIN")
    }

    @Test
    fun `websocket receives task created event`() = testApplication {
        application { module(testing = true) }
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"socketuser","password":"123456"}""")
        }
        val token = login("socketuser", "123456")
        val wsClient = createClient { install(WebSockets) }

        wsClient.webSocket("/ws/tasks") {
            val connected = (incoming.receive() as Frame.Text).readText()
            assertContains(connected, "CONNECTED")

            val create = client.post("/tasks") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"title":"WebSocket task","description":"test","done":false}""")
            }
            assertEquals(HttpStatusCode.Created, create.status)

            val event = (incoming.receive() as Frame.Text).readText()
            assertContains(event, "TASK_CREATED")
            assertContains(event, "WebSocket task")
        }
    }

    private suspend fun io.ktor.server.testing.ApplicationTestBuilder.login(login: String, password: String): String {
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"$login","password":"$password"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return json["token"]!!.jsonPrimitive.content
    }
}
