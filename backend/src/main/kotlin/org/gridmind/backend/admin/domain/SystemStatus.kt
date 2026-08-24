package org.gridmind.backend.admin.domain

/** One integration's status, for the admin status dashboard — never on any business path. */
data class IntegrationStatus(
    val name: String,
    val configured: Boolean,
    val detail: String? = null,
)

data class SystemStatus(
    val catalogProviders: List<IntegrationStatus>,
    val ollama: IntegrationStatus,
    val mqtt: IntegrationStatus,
)
