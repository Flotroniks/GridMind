package org.gridmind.backend.catalog.application

/**
 * Optional capability a [ProductCatalogProvider] can additionally implement when its API
 * offers a way to verify connectivity/credentials that isn't metered the same way a real
 * product search is — e.g. an OAuth2 client-credentials token endpoint, which DigiKey and
 * eBay both document as separate from their per-day search quota. Providers without such
 * an endpoint (Mouser's API is a single metered keyword-search call, nothing else) simply
 * don't implement this — see `SystemStatusService`, the only caller, for how that's
 * handled: "configured" (credentials present) rather than "verified reachable".
 */
interface CatalogProviderHealthCheck {
    /** Never throws — returns whether the provider is actually reachable with the
     * configured credentials, verified against a free endpoint. */
    fun checkHealth(): Boolean
}
