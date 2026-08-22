package org.gridmind.backend.shared.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * In-memory cache for external catalog search results — see
 * [org.gridmind.backend.catalog.application.ProductSearchService]. Purely a performance
 * optimization to avoid burning provider API quota on repeated searches: losing it on
 * restart is fine, so there's no need for anything beyond a single-process cache here.
 */
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager("catalogSearch").apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .expireAfterWrite(24, TimeUnit.HOURS)
                    .maximumSize(300),
            )
        }
}
