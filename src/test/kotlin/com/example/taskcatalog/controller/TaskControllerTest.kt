package com.example.taskcatalog.controller

import com.example.taskcatalog.dto.PageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.exception.GlobalExceptionHandler
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.service.TaskService
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [TaskController::class])
@Import(GlobalExceptionHandler::class)
class TaskControllerTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockitoBean
    lateinit var taskService: TaskService

    @Test
    fun `createTask returns 201 Created`() {
        given(taskService.createTask(any())).willReturn(Mono.just(sampleTaskResponse()))

        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"Prepare report","description":"Monthly financial report"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.status").isEqualTo("NEW")
    }

    @Test
    fun `createTask returns 400 for invalid title`() {
        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"ab","description":"short"}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Validation failed")
    }

    @Test
    fun `getTaskById returns 404 when task is absent`() {
        given(taskService.getTaskById(99L)).willReturn(Mono.error(TaskNotFoundException(99L)))

        webTestClient.get()
            .uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.message").isEqualTo("Task with id=99 was not found")
    }

    @Test
    fun `getTasks returns 200 OK with page payload`() {
        val response = PageResponse(
            content = listOf(sampleTaskResponse()),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
        )
        given(taskService.getTasks(0, 10, TaskStatus.NEW)).willReturn(Mono.just(response))

        webTestClient.get()
            .uri("/api/tasks?page=0&size=10&status=NEW")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.totalElements").isEqualTo(1)
    }

    @Test
    fun `getTasks returns 400 when required paging params are missing`() {
        webTestClient.get()
            .uri("/api/tasks")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `patchStatus returns 200 OK`() {
        val response = sampleTaskResponse(status = TaskStatus.DONE)
        given(taskService.updateStatus(any(), any())).willReturn(Mono.just(response))

        webTestClient.patch()
            .uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"status":"DONE"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("DONE")
    }

    @Test
    fun `deleteTask returns 204 No Content`() {
        given(taskService.deleteTask(1L)).willReturn(Mono.empty())

        webTestClient.delete()
            .uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }

    private fun sampleTaskResponse(
        status: TaskStatus = TaskStatus.NEW,
    ): TaskResponse = TaskResponse(
        id = 1L,
        title = "Prepare report",
        description = "Monthly financial report",
        status = status,
        createdAt = LocalDateTime.of(2026, 3, 26, 12, 0, 0),
        updatedAt = LocalDateTime.of(2026, 3, 26, 12, 0, 0),
    )
}
