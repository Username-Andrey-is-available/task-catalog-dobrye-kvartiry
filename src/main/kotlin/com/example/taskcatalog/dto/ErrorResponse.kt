package com.example.taskcatalog.dto

import java.time.OffsetDateTime

data class ErrorResponse(
    val timestamp: OffsetDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String> = emptyList(),
)
