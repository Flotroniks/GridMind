package org.gridmind.backend.admin.application

import org.gridmind.backend.admin.domain.IntegrationStatus
import org.gridmind.backend.admin.domain.SystemStatus
import org.gridmind.backend.catalog.application.ProductCatalogProvider
import org.gridmind.backend.locate.application.LocatePublisherPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Aggregates a point-in-time read of every external integration GridMind can talk to —
 * purely diagnostic, for the admin dashboard. Nothing here is on any business path: a
 * failing check here never breaks search, image analysis, or locate, it just reports
 * that something else would.
 */
@Service
class SystemStatusService(
    catalogProviders: List<ProductCatalogProvider>,
    private val ollamaDiagnosticsPort: OllamaDiagnosticsPort,
    private val locatePublisherPort: LocatePublisherPort,
    @Value("\${gridmind.imageanalysis.ollama.model}") private val configuredOllamaModel: String,
) {
    // Only the providers that actually registered as beans are here (each is gated by
    // its own @ConditionalOnProperty/@ConditionalOnExpression on credentials being
    // present) — so this list already *is* "which catalog providers are configured".
    private val configuredProviderNames = catalogProviders.map { it.name }.toSet()

    fun currentStatus(): SystemStatus =
        SystemStatus(
            catalogProviders = ALL_PROVIDER_NAMES.map { name ->
                IntegrationStatus(name = name, configured = name in configuredProviderNames)
            },
            ollama = ollamaStatus(),
            mqtt = mqttStatus(),
        )

    private fun ollamaStatus(): IntegrationStatus =
        try {
            val models = ollamaDiagnosticsPort.listAvailableModels()
            val modelLoaded = configuredOllamaModel in models
            IntegrationStatus(
                name = "Ollama",
                configured = modelLoaded,
                detail = if (modelLoaded) {
                    "Modèle '$configuredOllamaModel' disponible"
                } else {
                    "Serveur joignable, mais le modèle '$configuredOllamaModel' n'est pas pull"
                },
            )
        } catch (ex: Exception) {
            IntegrationStatus(name = "Ollama", configured = false, detail = "Serveur injoignable")
        }

    private fun mqttStatus(): IntegrationStatus {
        val connected = locatePublisherPort.isConnected()
        return IntegrationStatus(
            name = "MQTT",
            configured = connected,
            detail = if (connected) "Broker connecté" else "Broker injoignable",
        )
    }

    companion object {
        private val ALL_PROVIDER_NAMES = listOf("DigiKey", "Mouser", "Adafruit", "eBay")
    }
}
