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
import org.gxf.crestdevicesimulator.simulator.response.CommandService
import org.gxf.crestdevicesimulator.simulator.response.PskExtractor
import org.gxf.crestdevicesimulator.simulator.response.command.PskService
import org.gxf.crestdevicesimulator.simulator.response.handlers.CommandHandler
import org.springframework.stereotype.Service

@Service
class MessageHandler(
    private val coapClientService: CoapClientService,
    private val simulatorProperties: SimulatorProperties,
    private val pskService: PskService,
    private val mapper: ObjectMapper,
    private val commandService: CommandService,
    private val handlers: MutableList<out CommandHandler>,
    private val jacksonObjectMapper: ObjectMapper
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val URC_FIELD = "URC"
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "PSK:EQER"
        private const val REBOOT_SUCCESS = "INIT"
        private const val DL_FIELD = "DL"
    }

    fun sendMessage(simulatorState: SimulatorState): Boolean {
        val messageToBeSent = createMessageFromCurrentState(simulatorState)
        val request = createRequest(messageToBeSent)
        simulatorState.resetUrc()

        var coapClient: CoapClient? = null
        var immediateResponseRequested = false

        try {
            coapClient = coapClientService.createCoapClient()
            val response = coapClient.advanced(request)
            logger.info { "Received Response: ${response.payload.decodeToString()} with status ${response.code}" }
            handleResponse(response, simulatorState)
            immediateResponseRequested = String(response.payload).startsWith("!")
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun handleResponse(response: CoapResponse, simulatorState: SimulatorState) {
        if (response.isSuccess) {
            val payload = String(response.payload)
            when {
                PskExtractor.hasPskSetCommand(payload) -> {
                    handlePskSetCommand(payload, simulatorState)
                }
                pskService.isPendingKeyPresent() -> {
                    // Use new PSK with the next message, not in a response to the setter-msg TODO
                    // remove comment
                    pskService.changeActiveKey()
                }
                commandService.hasRebootCommand(payload) -> {
                    sendRebootSuccesMessage(payload, simulatorState)
                }
            }
            handlers.forEach { handler -> handler.handleResponse(response, simulatorState) }
        } else {
            logger.error { "Received error response with ${response.code}" }
            if (pskService.isPendingKeyPresent()) {
                logger.error { "Error received. Set pending key to invalid" }
                pskService.setPendingKeyAsInvalid()
            }
        }
    }

    private fun handlePskSetCommand(payload: String, simulatorState: SimulatorState) {
        try {
            logger.info { "Device ${simulatorProperties.pskIdentity} needs key change" }
            pskService.preparePendingKey(payload)
            sendPskSetSuccessMessage(payload, simulatorState)
        } catch (e: Exception) {
            logger.error(e) { "PSK change error, send failure message and set pending key status to invalid" }
            sendPskSetFailureMessage(payload, simulatorState)
            pskService.setPendingKeyAsInvalid()
        }
    }

    private fun sendPskSetSuccessMessage(pskCommand: String, simulatorState: SimulatorState) {
        logger.info { "Sending success message for command $pskCommand" }
        simulatorState.addUrc(URC_PSK_SUCCESS)
        simulatorState.addDownlink("PSK:####:SET")
        sendMessage(simulatorState)
    }

    private fun sendPskSetFailureMessage(pskCommand: String, simulatorState: SimulatorState) {
        logger.warn { "Sending failure message for command $pskCommand" }
        simulatorState.addUrc(URC_PSK_ERROR)
        simulatorState.addDownlink("PSK:####:SET")
        sendMessage(simulatorState)
    }

    private fun sendRebootSuccesMessage(command: String, simulatorState: SimulatorState) {
        logger.info { "Sending success message for command $command" }
        simulatorState.addUrc(REBOOT_SUCCESS)
        simulatorState.addDownlink("CMD:REBOOT")
        sendMessage(simulatorState)
    }
}
