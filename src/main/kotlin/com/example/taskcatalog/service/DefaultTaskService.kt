package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.PageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import java.time.Clock
import java.time.LocalDateTime
import kotlin.math.ceil
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class DefaultTaskService(
    private val taskRepository: TaskRepository,
    private val clock: Clock,
) : TaskService {

    override fun createTask(request: CreateTaskRequest): Mono<TaskResponse> = Mono.fromCallable {
        val now = LocalDateTime.now(clock)
        val taskToCreate = Task(
            id = null,
            title = request.title.trim(),
            description = request.description.normalizeDescription(),
            status = TaskStatus.NEW,
            createdAt = now,
            updatedAt = now,
        )

        TaskResponse.from(taskRepository.save(taskToCreate))
    }.subscribeOn(Schedulers.boundedElastic())

    override fun getTaskById(id: Long): Mono<TaskResponse> = Mono.fromCallable {
        val task = taskRepository.findById(id) ?: throw TaskNotFoundException(id)
        TaskResponse.from(task)
    }.subscribeOn(Schedulers.boundedElastic())

    override fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> = Mono.fromCallable {
        val tasks = taskRepository.findAll(page, size, status).map(TaskResponse::from)
        val totalElements = taskRepository.count(status)
        PageResponse(
            content = tasks,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = calculateTotalPages(totalElements, size),
        )
    }.subscribeOn(Schedulers.boundedElastic())

    override fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse> = Mono.fromCallable {
        val updatedRows = taskRepository.updateStatus(
            id = id,
            status = requireNotNull(request.status) { "status is required" },
            updatedAt = LocalDateTime.now(clock),
        )

        if (updatedRows == 0) {
            throw TaskNotFoundException(id)
        }

        TaskResponse.from(taskRepository.findById(id) ?: throw TaskNotFoundException(id))
    }.subscribeOn(Schedulers.boundedElastic())

    override fun deleteTask(id: Long): Mono<Void> = Mono.fromCallable {
        val deletedRows = taskRepository.deleteById(id)
        if (deletedRows == 0) {
            throw TaskNotFoundException(id)
        }
    }
        .subscribeOn(Schedulers.boundedElastic())
        .then()

    private fun calculateTotalPages(totalElements: Long, size: Int): Int {
        if (totalElements == 0L) {
            return 0
        }

        return ceil(totalElements.toDouble() / size.toDouble()).toInt()
    }

    private fun String?.normalizeDescription(): String? = this
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
