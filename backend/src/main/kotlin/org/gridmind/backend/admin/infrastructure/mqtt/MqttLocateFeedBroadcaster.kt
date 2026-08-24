package org.gridmind.backend.admin.infrastructure.mqtt

import jakarta.annotation.PreDestroy
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Fans out the locate topic to however many admin browser tabs are currently watching,
 * over one shared MQTT subscription rather than one per tab. Diagnostic-only, same as the
 * rest of the `admin` module: this never publishes anything and has no effect on the
 * locate feature itself (see `locate/infrastructure/mqtt/MqttLocatePublisher.kt` for the
 * publish side, which this is intentionally independent of).
 *
 * Connects lazily on the first registered emitter, not at startup, for the same reason as
 * `MqttLocatePublisher` — a broker that isn't up yet shouldn't fail application boot.
 */
@Component
class MqttLocateFeedBroadcaster(
    @Value("\${gridmind.locate.mqtt.broker-url}") private val brokerUrl: String,
    @Value("\${gridmind.locate.mqtt.topic}") private val topic: String,
) {
    private val logger = LoggerFactory.getLogger(MqttLocateFeedBroadcaster::class.java)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    @Volatile
    private var client: MqttClient? = null

    fun register(emitter: SseEmitter) {
        emitters += emitter
        val remove = { emitters.remove(emitter); Unit }
        emitter.onCompletion(remove)
        emitter.onTimeout(remove)
        emitter.onError { remove() }
        ensureSubscribed()
    }

    @Synchronized
    private fun ensureSubscribed() {
        if (client?.isConnected == true) return

        try {
            val fresh = MqttClient(brokerUrl, "gridmind-admin-live-${UUID.randomUUID()}", MemoryPersistence())
            fresh.setCallback(
                object : MqttCallback {
                    override fun messageArrived(receivedTopic: String, message: MqttMessage) =
                        broadcast(message.payload.toString(Charsets.UTF_8))

                    override fun connectionLost(cause: Throwable?) {
                        logger.warn("Admin MQTT live feed lost its connection: {}", cause?.message)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
                },
            )
            fresh.connect(MqttConnectOptions().apply { isCleanSession = true; isAutomaticReconnect = true })
            fresh.subscribe(topic)
            client = fresh
        } catch (ex: Exception) {
            logger.warn("Admin MQTT live feed could not connect to '{}': {}", brokerUrl, ex.message)
        }
    }

    private fun broadcast(payload: String) {
        for (emitter in emitters.toList()) {
            try {
                emitter.send(SseEmitter.event().name("locate").data(payload))
            } catch (ex: Exception) {
                emitters.remove(emitter)
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        client?.takeIf { it.isConnected }?.let { runCatching { it.disconnect() } }
        emitters.forEach { runCatching { it.complete() } }
    }
}
