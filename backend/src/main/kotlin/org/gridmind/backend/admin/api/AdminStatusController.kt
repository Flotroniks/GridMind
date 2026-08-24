package org.gridmind.backend.admin.api

import org.gridmind.backend.admin.application.SystemStatusService
import org.gridmind.backend.admin.domain.IntegrationStatus
import org.gridmind.backend.admin.domain.SystemStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/status")
class AdminStatusController(
    private val systemStatusService: SystemStatusService,
) {
    @GetMapping
    fun status(): SystemStatusResponse = SystemStatusResponse.from(systemStatusService.currentStatus())
}

data class IntegrationStatusResponse(
    val name: String,
    val configured: Boolean,
    val detail: String?,
) {
    companion object {
        fun from(status: IntegrationStatus): IntegrationStatusResponse =
            IntegrationStatusResponse(name = status.name, configured = status.configured, detail = status.detail)
    }
}

data class SystemStatusResponse(
    val catalogProviders: List<IntegrationStatusResponse>,
    val ollama: IntegrationStatusResponse,
    val mqtt: IntegrationStatusResponse,
) {
    companion object {
        fun from(status: SystemStatus): SystemStatusResponse = SystemStatusResponse(
            catalogProviders = status.catalogProviders.map(IntegrationStatusResponse::from),
            ollama = IntegrationStatusResponse.from(status.ollama),
            mqtt = IntegrationStatusResponse.from(status.mqtt),
        )
    }
}
