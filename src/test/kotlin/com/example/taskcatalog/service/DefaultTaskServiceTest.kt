package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class DefaultTaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2026-03-26T12:00:00Z"), ZoneOffset.UTC)

    private lateinit var taskService: DefaultTaskService

    @BeforeEach
    fun setUp() {
        taskService = DefaultTaskService(taskRepository, fixedClock)
    }

    @Test
    fun `createTask creates a new task with NEW status`() {
        val request = CreateTaskRequest(
            title = "Prepare report",
            description = "Monthly financial report",
        )
        val expectedTimestamp = LocalDateTime.of(2026, 3, 26, 12, 0, 0)
        val savedTask = Task(
            id = 1L,
            title = "Prepare report",
            description = "Monthly financial report",
            status = TaskStatus.NEW,
            createdAt = expectedTimestamp,
            updatedAt = expectedTimestamp,
        )

        whenever(taskRepository.save(any())).thenReturn(savedTask)

        StepVerifier.create(taskService.createTask(request))
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals(TaskStatus.NEW, response.status)
                assertEquals(expectedTimestamp, response.createdAt)
                assertEquals(expectedTimestamp, response.updatedAt)
            }
            .verifyComplete()

        val taskCaptor = argumentCaptor<Task>()
        verify(taskRepository).save(taskCaptor.capture())
        assertEquals("Prepare report", taskCaptor.firstValue.title)
        assertEquals("Monthly financial report", taskCaptor.firstValue.description)
        assertEquals(TaskStatus.NEW, taskCaptor.firstValue.status)
        assertEquals(expectedTimestamp, taskCaptor.firstValue.createdAt)
        assertEquals(expectedTimestamp, taskCaptor.firstValue.updatedAt)
    }

    @Test
    fun `getTaskById returns task when it exists`() {
        val task = sampleTask()
        whenever(taskRepository.findById(1L)).thenReturn(task)

        StepVerifier.create(taskService.getTaskById(1L))
            .assertNext { response ->
                assertEquals(task.id, response.id)
                assertEquals(task.title, response.title)
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById returns error when task does not exist`() {
        whenever(taskRepository.findById(42L)).thenReturn(null)

        StepVerifier.create(taskService.getTaskById(42L))
            .expectErrorSatisfies { error ->
                assertEquals(TaskNotFoundException::class, error::class)
                assertEquals("Task with id=42 was not found", error.message)
            }
            .verify()
    }

    @Test
    fun `updateStatus updates task status and timestamp`() {
        val updatedTimestamp = LocalDateTime.of(2026, 3, 26, 12, 0, 0)
        val updatedTask = sampleTask(
            status = TaskStatus.DONE,
            updatedAt = updatedTimestamp,
        )

        whenever(taskRepository.updateStatus(eq(1L), eq(TaskStatus.DONE), eq(updatedTimestamp))).thenReturn(1)
        whenever(taskRepository.findById(1L)).thenReturn(updatedTask)

        StepVerifier.create(taskService.updateStatus(1L, UpdateTaskStatusRequest(TaskStatus.DONE)))
            .assertNext { response ->
                assertEquals(TaskStatus.DONE, response.status)
                assertEquals(updatedTimestamp, response.updatedAt)
            }
            .verifyComplete()
    }

    @Test
    fun `deleteTask completes when task exists`() {
        whenever(taskRepository.deleteById(1L)).thenReturn(1)

        StepVerifier.create(taskService.deleteTask(1L))
            .verifyComplete()

        verify(taskRepository).deleteById(1L)
    }

    @Test
    fun `deleteTask returns error when task does not exist`() {
        whenever(taskRepository.deleteById(404L)).thenReturn(0)

        StepVerifier.create(taskService.deleteTask(404L))
            .expectErrorSatisfies { error ->
                assertEquals(TaskNotFoundException::class, error::class)
                assertEquals("Task with id=404 was not found", error.message)
            }
            .verify()
    }

    @Test
    fun `getTasks returns paged response with filtering`() {
        val task = sampleTask(status = TaskStatus.NEW)

        whenever(taskRepository.findAll(0, 10, TaskStatus.NEW)).thenReturn(listOf(task))
        whenever(taskRepository.count(TaskStatus.NEW)).thenReturn(11)

        StepVerifier.create(taskService.getTasks(0, 10, TaskStatus.NEW))
            .assertNext { page ->
                assertEquals(1, page.content.size)
                assertEquals(0, page.page)
                assertEquals(10, page.size)
                assertEquals(11L, page.totalElements)
                assertEquals(2, page.totalPages)
                assertEquals(TaskStatus.NEW, page.content.first().status)
            }
            .verifyComplete()
    }

    private fun sampleTask(
        id: Long = 1L,
        status: TaskStatus = TaskStatus.NEW,
        updatedAt: LocalDateTime = LocalDateTime.of(2026, 3, 26, 12, 0, 0),
    ): Task = Task(
        id = id,
        title = "Prepare report",
        description = "Monthly financial report",
        status = status,
        createdAt = LocalDateTime.of(2026, 3, 26, 12, 0, 0),
        updatedAt = updatedAt,
    )
}
