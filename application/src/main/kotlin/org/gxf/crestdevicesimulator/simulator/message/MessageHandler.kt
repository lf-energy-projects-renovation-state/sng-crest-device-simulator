// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.coap.Request
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.CborFactory
import org.gxf.crestdevicesimulator.simulator.coap.CoapClientService
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.event.MessageSentEvent
import org.gxf.crestdevicesimulator.simulator.response.handlers.CommandHandler
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class MessageHandler(
    private val coapClientService: CoapClientService,
    private val simulatorProperties: SimulatorProperties,
    private val handlers: MutableList<out CommandHandler>,
    private val jacksonObjectMapper: ObjectMapper,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger {}

    fun sendMessage(simulatorState: SimulatorState): Boolean {
        val messageToBeSent = createMessageFromCurrentState(simulatorState)
        val request = createRequest(messageToBeSent)
        simulatorState.resetUrc()

        var coapClient: CoapClient? = null
        var immediateResponseRequested = false

        try {
            coapClient = coapClientService.createCoapClient()
            val response = coapClient.advanced(request)
            if (response.isSuccess) {
                applicationEventPublisher.publishEvent(MessageSentEvent(messageToBeSent))
                immediateResponseRequested = handleResponse(response, simulatorState)
            } else {
                logger.error { "Received error response with ${response.code}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while trying to send a message." }
        } finally {
            if (coapClient != null) coapClientService.shutdownCoapClient(coapClient)
        }

        return immediateResponseRequested
    }

    private fun createMessageFromCurrentState(simulatorState: SimulatorState) =
        DeviceMessage().apply {
            fmc = simulatorState.fotaMessageCounter
            urc = simulatorState.getUrcListForDeviceMessage()
        }

    fun createRequest(message: DeviceMessage): Request {
        val jsonNode: JsonNode = jacksonObjectMapper.valueToTree(message)
        logger.info { "Sending request: $jsonNode" }
        val payload =
            if (simulatorProperties.produceValidCbor) CborFactory.createValidCbor(jsonNode)
            else CborFactory.createInvalidCbor()

        return Request.newPost()
            .apply { options.setContentFormat(MediaTypeRegistry.APPLICATION_CBOR) }
            .setPayload(payload)
    }

    private fun handleResponse(response: CoapResponse, simulatorState: SimulatorState): Boolean {
        val payload = String(response.payload)
        logger.info { "Received Response: $payload with status ${response.code}" }
        val immediateResponseRequested = payload.isImmediateResponseRequested()
        handleDownlinks(payload.stripImmediateResponseMarker(), simulatorState)
        return immediateResponseRequested
    }

    private fun String.isImmediateResponseRequested() = this.startsWith("!")

    private fun String.stripImmediateResponseMarker() = this.replace("^!".toRegex(), "")

    private fun handleDownlinks(downlinks: String, simulatorState: SimulatorState) {
        val commands = downlinks.split(";")
        commands.forEach { command ->
            handlers.forEach { handler ->
                if (handler.canHandleCommand(command)) handler.handleCommand(command, simulatorState)
            }
        }
    }
}
