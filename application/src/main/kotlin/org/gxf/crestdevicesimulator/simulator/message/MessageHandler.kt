// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
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
    private val handlers: List<CommandHandler>
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val URC_FIELD = "URC"
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "PSK:EQER"
        private const val DL_FIELD = "DL"
    }

    fun sendMessage(jsonNode: JsonNode, simulatorState: SimulatorState) {
        val newMessage = jsonNode as ObjectNode
        newMessage.replace("FMC", IntNode(simulatorState.fotaMessageCounter))
        logger.info { "Sending request: $newMessage" }
        val request = createRequest(newMessage)
        simulatorState.resetUrcs()

        var coapClient: CoapClient? = null

        try {
            coapClient = coapClientService.createCoapClient()
            val response = coapClient.advanced(request)
            logger.info { "Received Response: ${response.payload.decodeToString()} with status ${response.code}" }
            handleResponse(response, simulatorState)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coapClient != null) coapClientService.shutdownCoapClient(coapClient)
        }
    }

    fun createRequest(jsonNode: JsonNode): Request {
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
            if (payload != 0.toString()) {
                val newMessage = DeviceMessage(fmc = simulatorState.fotaMessageCounter)
                newMessage.urc = simulatorState.getUrcListForDeviceMessage()
                val jsonNode = mapper.valueToTree<JsonNode>(newMessage)
                sendMessage(jsonNode, simulatorState)
            }
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
        val messageJsonNode = mapper.readTree(simulatorProperties.successMessage.inputStream)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_SUCCESS, pskCommand)
        sendMessage(message, simulatorState)
    }

    private fun sendPskSetFailureMessage(pskCommand: String, simulatorState: SimulatorState) {
        logger.warn { "Sending failure message for command $pskCommand" }
        val messageJsonNode = mapper.readTree(simulatorProperties.failureMessage.inputStream)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_ERROR, pskCommand)
        sendMessage(message, simulatorState)
    }

    private fun sendRebootSuccesMessage(command: String, simulatorState: SimulatorState) {
        logger.info { "Sending success message for command $command" }
        val message = mapper.readTree(simulatorProperties.rebootSuccessMessage.inputStream)
        sendMessage(message, simulatorState)
    }

    private fun updatePskCommandInMessage(message: JsonNode, urc: String, receivedCommand: String): JsonNode {
        val newMessage = message as ObjectNode
        val urcList =
            listOf(TextNode(urc), ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(receivedCommand))))
        val urcArray = mapper.valueToTree<ArrayNode>(urcList)
        newMessage.replace(URC_FIELD, urcArray)
        logger.debug { "Sending message with URC $urcArray" }
        return newMessage
    }
}
