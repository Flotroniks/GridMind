package org.gridmind.backend.catalog.infrastructure.provider.mouser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/** Confirms the bean only registers when a Mouser API key is actually set. */
class MouserProductCatalogProviderConditionalTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(MouserProductCatalogProvider::class.java)

    @Test
    fun `bean is absent when no api key is configured`() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(MouserProductCatalogProvider::class.java)
        }
    }

    @Test
    fun `bean is absent when the api key is blank`() {
        contextRunner
            .withPropertyValues("gridmind.catalog.mouser.api-key=")
            .run { context ->
                assertThat(context).doesNotHaveBean(MouserProductCatalogProvider::class.java)
            }
    }

    @Test
    fun `bean is present when the api key is configured`() {
        contextRunner
            .withPropertyValues(
                "gridmind.catalog.mouser.base-url=https://api.mouser.com",
                "gridmind.catalog.mouser.api-key=test-key",
            )
            .run { context ->
                assertThat(context).hasSingleBean(MouserProductCatalogProvider::class.java)
            }
    }
}
