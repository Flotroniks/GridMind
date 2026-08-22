package org.gridmind.backend.shared.error

data class ErrorResponse(
    val status: Int,
    val message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
)
