package org.example.errors

import io.ktor.http.HttpStatusCode

open class ApiException(
    val status: HttpStatusCode,
    override val message: String
) : RuntimeException(message)

class BadRequestApiException(message: String) :
    ApiException(HttpStatusCode.BadRequest, message)

class UnauthorizedApiException(message: String) :
    ApiException(HttpStatusCode.Unauthorized, message)

class ForbiddenApiException(message: String) :
    ApiException(HttpStatusCode.Forbidden, message)

class NotFoundApiException(message: String) :
    ApiException(HttpStatusCode.NotFound, message)

class ConflictApiException(message: String) :
    ApiException(HttpStatusCode.Conflict, message)
