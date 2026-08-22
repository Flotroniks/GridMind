package org.gridmind.backend.catalog.infrastructure.provider.digikey

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/** Confirms the bean only registers when both DigiKey credentials are actually set. */
class DigiKeyProductCatalogProviderConditionalTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(DigiKeyProductCatalogProvider::class.java)

    @Test
    fun `bean is absent when no credentials are configured`() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(DigiKeyProductCatalogProvider::class.java)
        }
    }

    @Test
    fun `bean is absent when only one credential is configured`() {
        contextRunner
            .withPropertyValues("gridmind.catalog.digikey.client-id=test-id")
            .run { context ->
                assertThat(context).doesNotHaveBean(DigiKeyProductCatalogProvider::class.java)
            }
    }

    @Test
    fun `bean is present when both credentials are configured`() {
        contextRunner
            .withPropertyValues(
                "gridmind.catalog.digikey.base-url=https://sandbox-api.digikey.com",
                "gridmind.catalog.digikey.client-id=test-id",
                "gridmind.catalog.digikey.client-secret=test-secret",
            )
            .run { context ->
                assertThat(context).hasSingleBean(DigiKeyProductCatalogProvider::class.java)
            }
    }
}
