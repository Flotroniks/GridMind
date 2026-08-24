package org.gridmind.backend.admin.api

import org.gridmind.backend.admin.infrastructure.mqtt.MqttLocateFeedBroadcaster
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/admin/locate")
class AdminLocateStreamController(
    private val broadcaster: MqttLocateFeedBroadcaster,
) {
    /** A live view of the `gridmind/locate` MQTT topic, relayed to the browser over SSE
     * since raw MQTT isn't reachable from a browser tab. No timeout — the connection is
     * meant to stay open for as long as the admin tab is. */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): SseEmitter {
        val emitter = SseEmitter(0L)
        broadcaster.register(emitter)
        return emitter
    }
}
