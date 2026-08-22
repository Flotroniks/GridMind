package org.gridmind.backend.catalog.infrastructure.provider.adafruit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/** Confirms the bean registers by default (no credentials needed) but can be turned off. */
class AdafruitProductCatalogProviderConditionalTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(AdafruitProductCatalogProvider::class.java)
        .withPropertyValues("gridmind.catalog.adafruit.base-url=https://www.adafruit.com")

    @Test
    fun `bean is present when nothing is configured`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(AdafruitProductCatalogProvider::class.java)
        }
    }

    @Test
    fun `bean is present when explicitly enabled`() {
        contextRunner
            .withPropertyValues("gridmind.catalog.adafruit.enabled=true")
            .run { context ->
                assertThat(context).hasSingleBean(AdafruitProductCatalogProvider::class.java)
            }
    }

    @Test
    fun `bean is absent when explicitly disabled`() {
        contextRunner
            .withPropertyValues("gridmind.catalog.adafruit.enabled=false")
            .run { context ->
                assertThat(context).doesNotHaveBean(AdafruitProductCatalogProvider::class.java)
            }
    }
}
