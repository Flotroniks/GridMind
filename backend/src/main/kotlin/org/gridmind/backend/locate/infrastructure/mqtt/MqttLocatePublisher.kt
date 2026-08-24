package org.gridmind.backend.locate.infrastructure.mqtt

import jakarta.annotation.PreDestroy
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.gridmind.backend.locate.application.LocatePublisherPort
import org.gridmind.backend.locate.domain.LocateHighlight
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/**
 * Publishes the current highlight state as one retained-free JSON message on a single
 * fixed topic — no per-location topics, no command/acknowledgement handshake. Whatever
 * subscribes (an addressable LED controller, eventually — out of scope here, see the
 * README's "Locate / MQTT" section) is expected to replace its lit set with exactly what
 * this message says, nothing more.
 *
 * Connects lazily on the first publish rather than at startup, so a broker that isn't up
 * yet doesn't fail application boot — and every publish is wrapped so a broken/unreachable
 * broker degrades to "the lights don't update" instead of breaking inventory search, which
 * triggers this as an on-the-side effect and must keep working regardless.
 */
@Component
class MqttLocatePublisher(
    @Value("\${gridmind.locate.mqtt.broker-url}") private val brokerUrl: String,
    @Value("\${gridmind.locate.mqtt.client-id}") private val clientId: String,
    @Value("\${gridmind.locate.mqtt.topic}") private val topic: String,
    private val objectMapper: ObjectMapper,
) : LocatePublisherPort {
    private val logger = LoggerFactory.getLogger(MqttLocatePublisher::class.java)

    @Volatile
    private var client: MqttClient? = null

    override fun publish(highlights: List<LocateHighlight>) {
        try {
            val payload = objectMapper.writeValueAsBytes(LocateMqttPayload.from(highlights))
            connectedClient().publish(topic, MqttMessage(payload).apply { qos = 0 })
        } catch (ex: Exception) {
            logger.warn("Failed to publish locate highlight to MQTT broker '{}': {}", brokerUrl, ex.message)
        }
    }

    @Synchronized
    private fun connectedClient(): MqttClient {
        val existing = client
        if (existing != null && existing.isConnected) return existing

        val fresh = MqttClient(brokerUrl, clientId, MemoryPersistence()).apply {
            connect(
                MqttConnectOptions().apply {
                    isCleanSession = true
                    isAutomaticReconnect = true
                    connectionTimeout = 5
                },
            )
        }
        client = fresh
        return fresh
    }

    @PreDestroy
    fun disconnect() {
        client?.takeIf { it.isConnected }?.let { runCatching { it.disconnect() } }
    }
}

internal data class LocateMqttPayload(val locations: List<LocateMqttLocation>) {
    companion object {
        fun from(highlights: List<LocateHighlight>): LocateMqttPayload =
            LocateMqttPayload(
                highlights.map {
                    LocateMqttLocation(id = it.storageLocationId, name = it.storageLocationName, color = it.color)
                },
            )
    }
}

internal data class LocateMqttLocation(val id: Long, val name: String, val color: String)
