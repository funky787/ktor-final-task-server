package org.example.repository

import org.example.database.TasksTable
import org.example.dto.CreateTaskRequest
import org.example.dto.UpdateTaskRequest
import org.example.model.Task
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object TaskRepository {
    fun getAll(): List<Task> = transaction {
        TasksTable.selectAll().map(::toTask)
    }

    fun getById(id: Int): Task? = transaction {
        TasksTable.select { TasksTable.id eq id }
            .limit(1)
            .map(::toTask)
            .singleOrNull()
    }

    fun create(request: CreateTaskRequest, ownerId: Int): Task = transaction {
        val statement = TasksTable.insert {
            it[title] = request.title
            it[description] = request.description
            it[done] = request.done
            it[TasksTable.ownerId] = ownerId
        }

        Task(
            id = statement[TasksTable.id],
            title = request.title,
            description = request.description,
            done = request.done,
            ownerId = ownerId
        )
    }

    fun update(id: Int, request: UpdateTaskRequest): Task? = transaction {
        val changed = TasksTable.update({ TasksTable.id eq id }) {
            it[title] = request.title
            it[description] = request.description
            it[done] = request.done
        }
        if (changed == 0) null else getById(id)
    }

    fun delete(id: Int): Boolean = transaction {
        TasksTable.deleteWhere { TasksTable.id eq id } > 0
    }

    private fun toTask(row: ResultRow) = Task(
        id = row[TasksTable.id],
        title = row[TasksTable.title],
        description = row[TasksTable.description],
        done = row[TasksTable.done],
        ownerId = row[TasksTable.ownerId]
    )
}
