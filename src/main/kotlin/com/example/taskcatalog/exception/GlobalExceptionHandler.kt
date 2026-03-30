package com.example.taskcatalog.exception

import com.example.taskcatalog.dto.ErrorResponse
import java.time.OffsetDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(
        exception: TaskNotFoundException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> = buildErrorResponse(
        status = HttpStatus.NOT_FOUND,
        message = exception.message ?: "Task not found",
        path = exchange.request.path.value(),
    )

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(
        exception: WebExchangeBindException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> {
        val details = exception.bindingResult.allErrors.mapNotNull { error ->
            when (error) {
                is FieldError -> error.defaultMessage?.let { "${error.field}: $it" }
                else -> error.defaultMessage
            }
        }

        return buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = exchange.request.path.value(),
            details = details,
        )
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidationException(
        exception: HandlerMethodValidationException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> {
        val parameterDetails = exception.parameterValidationResults.flatMap { validationResult ->
            validationResult.resolvableErrors.map { error ->
                error.defaultMessage ?: "Validation failed"
            }
        }
        val crossParameterDetails = exception.crossParameterValidationResults.map { error ->
            error.defaultMessage ?: "Validation failed"
        }
        val details = parameterDetails + crossParameterDetails

        return buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = exchange.request.path.value(),
            details = details.ifEmpty { listOf("Validation failed") },
        )
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(
        exception: ServerWebInputException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> = buildErrorResponse(
        status = HttpStatus.BAD_REQUEST,
        message = exception.reason ?: "Invalid request",
        path = exchange.request.path.value(),
    )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> = buildErrorResponse(
        status = HttpStatus.BAD_REQUEST,
        message = exception.message ?: "Invalid request",
        path = exchange.request.path.value(),
    )

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(
        exception: Exception,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> = buildErrorResponse(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        message = "Internal server error",
        path = exchange.request.path.value(),
    )

    private fun buildErrorResponse(
        status: HttpStatus,
        message: String,
        path: String,
        details: List<String> = emptyList(),
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(
        ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            path = path,
            details = details,
        ),
    )
}
