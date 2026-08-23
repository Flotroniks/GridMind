package org.gridmind.backend.catalog.infrastructure.provider.ebay

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/** Confirms the bean only registers when both eBay credentials are actually set. */
class EbayProductCatalogProviderConditionalTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(EbayProductCatalogProvider::class.java)

    @Test
    fun `bean is absent when no credentials are configured`() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(EbayProductCatalogProvider::class.java)
        }
    }

    @Test
    fun `bean is absent when only one credential is configured`() {
        contextRunner
            .withPropertyValues("gridmind.catalog.ebay.client-id=test-id")
            .run { context ->
                assertThat(context).doesNotHaveBean(EbayProductCatalogProvider::class.java)
            }
    }

    @Test
    fun `bean is present when both credentials are configured`() {
        contextRunner
            .withPropertyValues(
                "gridmind.catalog.ebay.base-url=https://api.ebay.com",
                "gridmind.catalog.ebay.client-id=test-id",
                "gridmind.catalog.ebay.client-secret=test-secret",
                "gridmind.catalog.ebay.marketplace-id=EBAY_US",
            )
            .run { context ->
                assertThat(context).hasSingleBean(EbayProductCatalogProvider::class.java)
            }
    }
}
