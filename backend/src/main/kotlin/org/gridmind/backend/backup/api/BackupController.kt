package org.gridmind.backend.backup.api

import org.gridmind.backend.backup.application.BackupService
import org.gridmind.backend.backup.application.BackupSnapshot
import org.gridmind.backend.backup.application.BackupSummary
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/backup")
class BackupController(
    private val backupService: BackupService,
) {
    @GetMapping("/export")
    fun export(): ResponseEntity<BackupSnapshot> {
        val filename = "gridmind-backup-${LocalDate.now()}.json"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .body(backupService.export())
    }

    /** Always a full restore — see [BackupService.import] for why a partial/merge import
     * isn't offered: replacing everything sidesteps every id/uniqueness conflict a merge
     * would otherwise have to resolve. */
    @PostMapping("/import")
    fun import(@RequestBody snapshot: BackupSnapshot): BackupSummary = backupService.import(snapshot)

    @PostMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clear() {
        backupService.clear()
    }
}
