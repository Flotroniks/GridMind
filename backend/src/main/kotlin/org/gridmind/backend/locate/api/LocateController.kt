package org.gridmind.backend.locate.api

import org.gridmind.backend.locate.application.LocateService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class LocateController(
    private val locateService: LocateService,
) {
    /** Fire-and-forget from the frontend's point of view — the response carries nothing
     * back, and a failure to reach the MQTT broker is swallowed inside [LocateService]'s
     * publisher rather than surfaced as an error, since this is a side effect of search,
     * not something the search itself should ever fail over. */
    @PostMapping("/api/locate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun locate(@RequestParam(required = false) query: String?) {
        locateService.locate(query)
    }
}
