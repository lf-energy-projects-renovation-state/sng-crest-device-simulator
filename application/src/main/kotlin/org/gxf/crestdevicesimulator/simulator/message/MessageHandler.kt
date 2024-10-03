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

    fun sendMessage(jsonNode: JsonNode) {
        val request = createRequest(jsonNode)
        logger.info { "Sending request: $request" }

        var coapClient: CoapClient? = null

        try {
            coapClient = coapClientService.createCoapClient()
            val response = coapClient.advanced(request)
            logger.info { "Received Response: ${response.payload.decodeToString()} with status ${response.code}" }
            handleResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coapClient != null) coapClientService.shutdownCoapClient(coapClient)
        }
    }

    fun createRequest(jsonNode: JsonNode): Request {
        val newMessage = jsonNode as ObjectNode
        newMessage.replace("FMC", IntNode(simulatorProperties.fotaMessageCounter))
        val payload =
            if (simulatorProperties.produceValidCbor) CborFactory.createValidCbor(newMessage)
            else CborFactory.createInvalidCbor()

        return Request.newPost()
            .apply { options.setContentFormat(MediaTypeRegistry.APPLICATION_CBOR) }
            .setPayload(payload)
    }

    private fun handleResponse(response: CoapResponse) {
        if (response.isSuccess) {
            val payload = String(response.payload)
            when {
                PskExtractor.hasPskSetCommand(payload) -> {
                    handlePskSetCommand(payload)
                }
                pskService.isPendingKeyPresent() -> {
                    // Use new PSK with the next message, not in a response to the setter-msg TODO
                    // remove comment
                    pskService.changeActiveKey()
                }
                commandService.hasRebootCommand(payload) -> {
                    sendRebootSuccesMessage(payload)
                }
            }
            val returnCodes = handlers.map { handler -> handler.handleResponse(response, simulatorProperties) }
            if (payload != 0.toString()) {
                val newMessage = DeviceMessage(FMC = simulatorProperties.fotaMessageCounter)
                newMessage.setURC(returnCodes, payload)
                val jsonNode = mapper.valueToTree<JsonNode>(newMessage)
                sendMessage(jsonNode)
            }
        } else {
            logger.error { "Received error response with ${response.code}" }
            if (pskService.isPendingKeyPresent()) {
                logger.error { "Error received. Set pending key to invalid" }
                pskService.setPendingKeyAsInvalid()
            }
        }
    }

    private fun handlePskSetCommand(payload: String) {
        try {
            logger.info { "Device ${simulatorProperties.pskIdentity} needs key change" }
            pskService.preparePendingKey(payload)
            sendPskSetSuccessMessage(payload)
        } catch (e: Exception) {
            logger.error(e) { "PSK change error, send failure message and set pending key status to invalid" }
            sendPskSetFailureMessage(payload)
            pskService.setPendingKeyAsInvalid()
        }
    }

    private fun sendPskSetSuccessMessage(pskCommand: String) {
        logger.info { "Sending success message for command $pskCommand" }
        val messageJsonNode = mapper.readTree(simulatorProperties.successMessage.inputStream)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_SUCCESS, pskCommand)
        sendMessage(message)
    }

    private fun sendPskSetFailureMessage(pskCommand: String) {
        logger.warn { "Sending failure message for command $pskCommand" }
        val messageJsonNode = mapper.readTree(simulatorProperties.failureMessage.inputStream)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_ERROR, pskCommand)
        sendMessage(message)
    }

    private fun sendRebootSuccesMessage(command: String) {
        logger.info { "Sending success message for command $command" }
        val message = mapper.readTree(simulatorProperties.rebootSuccessMessage.inputStream)
        sendMessage(message)
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
