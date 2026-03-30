package com.example.taskcatalog.config

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import java.time.Clock
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("seed")
class SampleDataSeeder(
    private val taskRepository: TaskRepository,
    private val clock: Clock,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (taskRepository.count(null) > 0) {
            logger.info("Sample data seeding skipped because tasks table is not empty")
            return
        }

        val now = LocalDateTime.now(clock)
        val sampleTasks = listOf(
            sampleTask(
                title = "Prepare monthly report",
                description = "Collect finance metrics and prepare summary slides",
                status = TaskStatus.NEW,
                createdAt = now.minusDays(7),
                updatedAt = now.minusDays(7),
            ),
            sampleTask(
                title = "Call supplier",
                description = "Discuss delays in office equipment delivery",
                status = TaskStatus.IN_PROGRESS,
                createdAt = now.minusDays(6),
                updatedAt = now.minusDays(2),
            ),
            sampleTask(
                title = "Fix login bug",
                description = "Investigate authorization failure after token refresh",
                status = TaskStatus.DONE,
                createdAt = now.minusDays(5),
                updatedAt = now.minusDays(1),
            ),
            sampleTask(
                title = "Update landing page",
                description = "Replace old screenshots and refresh feature text",
                status = TaskStatus.NEW,
                createdAt = now.minusDays(4),
                updatedAt = now.minusDays(4),
            ),
            sampleTask(
                title = "Plan team meetup",
                description = "Choose date, venue, and collect headcount",
                status = TaskStatus.CANCELLED,
                createdAt = now.minusDays(3),
                updatedAt = now.minusHours(20),
            ),
            sampleTask(
                title = "Prepare onboarding checklist",
                description = "Create starter tasks for the new backend developer",
                status = TaskStatus.IN_PROGRESS,
                createdAt = now.minusDays(2),
                updatedAt = now.minusHours(10),
            ),
            sampleTask(
                title = "Review pull requests",
                description = "Check open backend PRs before release branch cut",
                status = TaskStatus.NEW,
                createdAt = now.minusHours(30),
                updatedAt = now.minusHours(30),
            ),
            sampleTask(
                title = "Archive old invoices",
                description = "Move processed invoices to long-term storage",
                status = TaskStatus.DONE,
                createdAt = now.minusHours(18),
                updatedAt = now.minusHours(8),
            ),
            sampleTask(
                title = "Write API guide",
                description = "Document task service endpoints for QA and frontend",
                status = TaskStatus.IN_PROGRESS,
                createdAt = now.minusHours(8),
                updatedAt = now.minusHours(2),
            ),
            sampleTask(
                title = "Prepare release checklist",
                description = "List smoke tests and deployment validation steps",
                status = TaskStatus.NEW,
                createdAt = now.minusHours(1),
                updatedAt = now.minusHours(1),
            ),
        )

        sampleTasks.forEach(taskRepository::save)
        logger.info("Inserted {} sample tasks", sampleTasks.size)
    }

    private fun sampleTask(
        title: String,
        description: String,
        status: TaskStatus,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ): Task = Task(
        id = null,
        title = title,
        description = description,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        private val logger = LoggerFactory.getLogger(SampleDataSeeder::class.java)
    }
}
