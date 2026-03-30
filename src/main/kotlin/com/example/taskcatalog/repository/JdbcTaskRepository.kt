package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import java.sql.ResultSet
import java.time.LocalDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
class JdbcTaskRepository(
    private val jdbcClient: JdbcClient,
) : TaskRepository {

    override fun save(task: Task): Task {
        val keyHolder = GeneratedKeyHolder()

        jdbcClient.sql(
            """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
            """.trimIndent(),
        )
            .param("title", task.title)
            .param("description", task.description)
            .param("status", task.status.name)
            .param("createdAt", task.createdAt)
            .param("updatedAt", task.updatedAt)
            .update(keyHolder, "id")

        val createdId = keyHolder.key?.toLong()
            ?: throw IllegalStateException("Unable to read generated key for task")

        return findById(createdId)
            ?: throw IllegalStateException("Task with id=$createdId was inserted but could not be loaded")
    }

    override fun findById(id: Long): Task? = jdbcClient.sql(
        """
        SELECT id, title, description, status, created_at, updated_at
        FROM tasks
        WHERE id = :id
        """.trimIndent(),
    )
        .param("id", id)
        .query(taskRowMapper)
        .optional()
        .orElse(null)

    override fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task> {
        val sql = buildString {
            append(
                """
                SELECT id, title, description, status, created_at, updated_at
                FROM tasks
                """.trimIndent(),
            )

            if (status != null) {
                append("\nWHERE status = :status")
            }

            append("\nORDER BY created_at DESC")
            append("\nLIMIT :limit OFFSET :offset")
        }

        var statement = jdbcClient.sql(sql)
            .param("limit", size)
            .param("offset", page * size)

        if (status != null) {
            statement = statement.param("status", status.name)
        }

        return statement.query(taskRowMapper).list()
    }

    override fun count(status: TaskStatus?): Long {
        val sql = buildString {
            append("SELECT COUNT(*) FROM tasks")
            if (status != null) {
                append(" WHERE status = :status")
            }
        }

        var statement = jdbcClient.sql(sql)
        if (status != null) {
            statement = statement.param("status", status.name)
        }

        return statement.query(Long::class.javaObjectType).single() ?: 0L
    }

    override fun updateStatus(id: Long, status: TaskStatus, updatedAt: LocalDateTime): Int = jdbcClient.sql(
        """
        UPDATE tasks
        SET status = :status,
            updated_at = :updatedAt
        WHERE id = :id
        """.trimIndent(),
    )
        .param("id", id)
        .param("status", status.name)
        .param("updatedAt", updatedAt)
        .update()

    override fun deleteById(id: Long): Int = jdbcClient.sql(
        """
        DELETE FROM tasks
        WHERE id = :id
        """.trimIndent(),
    )
        .param("id", id)
        .update()

    private val taskRowMapper = RowMapper<Task> { rs, _ -> rs.toTask() }

    private fun ResultSet.toTask(): Task = Task(
        id = getLong("id"),
        title = getString("title"),
        description = getString("description"),
        status = TaskStatus.valueOf(getString("status")),
        createdAt = getObject("created_at", LocalDateTime::class.java),
        updatedAt = getObject("updated_at", LocalDateTime::class.java),
    )
}
