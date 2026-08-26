package org.gridmind.backend.backup.api

import org.gridmind.backend.backup.application.BackupService
import org.gridmind.backend.backup.application.BackupSnapshot
import org.gridmind.backend.backup.application.BackupSummary
import org.gridmind.backend.backup.application.CategorySnapshot
import org.gridmind.backend.shared.config.SecurityConfig
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@WebMvcTest(BackupController::class)
@Import(SecurityConfig::class)
class BackupControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var backupService: BackupService

    @Test
    fun `export returns 200 with a JSON attachment`() {
        `when`(backupService.export()).thenReturn(BackupSnapshot(categories = listOf(CategorySnapshot(id = 1L, name = "Sensors"))))

        mockMvc.perform(get("/api/backup/export"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Disposition", containsString("attachment")))
            .andExpect(jsonPath("$.categories[0].name").value("Sensors"))
    }

    @Test
    fun `import returns 200 with a summary`() {
        // A concrete value rather than any(BackupSnapshot::class.java): the latter trips
        // Kotlin's null-check on import()'s non-null parameter when BackupService (a
        // concrete class, not an interface) is mocked — a fixed Instant keeps this value
        // identical after the JSON round-trip below, so equals()-based stubbing matches.
        val snapshot = BackupSnapshot(exportedAt = Instant.EPOCH)
        `when`(backupService.import(snapshot))
            .thenReturn(BackupSummary(categories = 1, storageLocations = 0, images = 0, items = 0, itemStock = 0))

        mockMvc.perform(
            post("/api/backup/import")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(snapshot)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.categories").value(1))
    }

    @Test
    fun `clear returns 204`() {
        mockMvc.perform(post("/api/backup/clear"))
            .andExpect(status().isNoContent)
    }
}
