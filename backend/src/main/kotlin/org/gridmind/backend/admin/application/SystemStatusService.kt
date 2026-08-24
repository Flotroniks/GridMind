package org.gridmind.backend.admin.application

import org.gridmind.backend.admin.domain.IntegrationStatus
import org.gridmind.backend.admin.domain.SystemStatus
import org.gridmind.backend.catalog.application.CatalogProviderHealthCheck
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
    // present) — so this map already *is* "which catalog providers are configured".
    private val providersByName = catalogProviders.associateBy { it.name }

    fun currentStatus(): SystemStatus =
        SystemStatus(
            catalogProviders = ALL_PROVIDER_NAMES.map(::catalogProviderStatus),
            ollama = ollamaStatus(),
            mqtt = mqttStatus(),
        )

    /**
     * For a provider that implements [CatalogProviderHealthCheck] (DigiKey, eBay,
     * Adafruit — each has a way to verify connectivity that isn't metered like a real
     * search), this reports genuine live reachability. Mouser has no such endpoint — its
     * only API call is the metered search itself — so it stays "configured" (credentials
     * present) without a live check, rather than burning search quota just to render a
     * dashboard.
     */
    private fun catalogProviderStatus(name: String): IntegrationStatus {
        val provider = providersByName[name] ?: return IntegrationStatus(name = name, configured = false)

        if (provider !is CatalogProviderHealthCheck) {
            return IntegrationStatus(
                name = name,
                configured = true,
                detail = "Identifiants présents — connectivité non vérifiable (pas d'endpoint gratuit)",
            )
        }

        val reachable = provider.checkHealth()
        return IntegrationStatus(
            name = name,
            configured = reachable,
            detail = if (reachable) "Identifiants valides, joignable" else "Identifiants rejetés ou service injoignable",
        )
    }

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
